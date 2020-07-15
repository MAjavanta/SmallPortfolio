package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

//sub class of drone (super class)
public class StatelessDrone extends Drone {
	private Map<Double, Node> distNodeMap;
	private ArrayList<Node> mapNodes;

	// stateless drone constructer
	public StatelessDrone(Position initPos, long randomSeed) {
		super(initPos);
		this.randomSeed = new Random(randomSeed); // random seed so that random walk is reproducible 
	}

	// play method carries out the algorithm
	// while the drone is alive, look ahead 1 step, and move towards best charging station in that radius,
	// if no good node's can be found. take a random step in any direction (random walk w/ 1 step look ahead)
	public void play(ArrayList<Node> mapNodes) {
		this.mapNodes = mapNodes;
		
		// while the drone is alive...
		while (isAlive() && this.movesMadeSoFar.size() <= this.maxMovesAllowed) {
			// create a map (key, value pairs) where the key is the 
			// distance from currentPos to a node and value is the node object
			// this map is then sorted by key (distances) in ascending order
			// then the closest (within 1 step radius) green node is chosen as a target
			// drone will then move to this target (if it is in 1 step range)
			// otherwise a random direction is chosen
			
			// unlike the stateful drone, this distance-node hashmap only contains nodes in range within one step
			// where as stateful has a distance-node map which contains all nodes in the map
			Map<Double, Node> unsortedDistNodes = getUnsortedDistNodes(); // create unsorted distance-node map
			this.distNodeMap = sortDistNodes(unsortedDistNodes);		  // sort this map using key, smallest dist first...
			Node closestGoodNode = getClosestGreenNode(); 				  // get closest green charging station from this map
			
			// if the closest green node is null
			Position bestNextMove;
			Direction dirToMoveIn = null;
			if (closestGoodNode == null) {
				// this means that there was no good green nodes in range and the drone should take a random step
				List<Direction> possibleNextDirs = new Direction().getAllDirs();		// get a list of all possible directions
				dirToMoveIn = getRandomDirs(possibleNextDirs);					// randomly pick a direction
				bestNextMove = this.currentPos.nextPosition(dirToMoveIn);;    	// and move in that direction

			} else {
				// otherwise, the drone has a found a green charging station within range and will move towards it
				// by finding the angle (from east) between current position and node's position, snapping the angle
				// to 1 of 16 possible directions and moving
				double angle = this.currentPos.getAngleBetween(closestGoodNode.pos);
				dirToMoveIn = new Direction().snapDir(angle);
				bestNextMove = this.currentPos.nextPosition(dirToMoveIn);
			}
			
			moveTo(bestNextMove, dirToMoveIn);
		}
	}

	// return a map (key-value pairs) where the key is the distance to a node and the value is the node itself
	public Map<Double, Node> getUnsortedDistNodes() {
		// create a new map object (double for dist, node for charging stations)
		Map<Double, Node> unsortedDistNodes = new HashMap<Double, Node>();	
		// for each node, n in mapNodes (list of all the nodes)...
		for (Node node : this.mapNodes) {
			// if n has not already been used...
			if (!node.used) {
				// get distance, d from current position to n's position
				// if d<step (drone can only "see" a step out)
				// then add d and n to map, we only need to consider nodes in range, why consider all?
				double fromCurrDist = this.currentPos.getL2Dist(node.pos); 			
				Boolean nInsideGreaterCircle = fromCurrDist <= this.currentPos.getStep();//+node.getRadius();
				
				// keep in mind just because a node is within this greater circle with r=step
				// doesnt mean that the drone can actually reach it in one step as radius for a node is smaller than a step
				if (nInsideGreaterCircle) { 									
					unsortedDistNodes.put(fromCurrDist, node);
				}
			}
		}
		return unsortedDistNodes;
	}
	
	// Map<Double, Node> unsortedDistNodes is sorted by key (distance,
	// from a node within radius of effect and next possible move)
	public Map<Double, Node> sortDistNodes(Map<Double, Node> unsortedDistNodes) {
        Map<Double, Node> distNodes = new TreeMap<Double, Node>(new Comparator<Double>()
        { @Override
        	// compare is overridden so that sort is ascending order
            public int compare(Double i, Double j) {
                return i.compareTo(j);
            }
        });
        distNodes.putAll(unsortedDistNodes); // sort occurs when unsorted map is put into new map (since new comparator)
        return distNodes;
	}
	
	// returns the the closest green charging station by doing weight comparisons
	public Node getClosestGreenNode() {
		// linear search through the map to find green node, since map is already sorted by key (distance)
		// we can be sure that the first node with weight>0 (green) is the closest green node
		Node bestNode = null;
		
		// for every node, n in the map...
		for (Entry<Double, Node> entry : this.distNodeMap.entrySet()) {
			Node node = entry.getValue();
			// check if n is green, break out of loop and return n (as it's the closest green charging station) 
			if (node.weight > 0) {
				bestNode = node;
				break;
			}
		}
		return bestNode;
	}
	
	// makes drone move to a specific position
	public void moveTo(Position pos, Direction dirToMoveIn) {
		// move only occurs if drone is still alive and the position 
		// to be moved to, pos is in the playable area
		
		if (isAlive() && pos.inPlayArea()) {
			// power reduced by the power consumption (step cost)
			// current position is updated to new pos
			// position added to movesMadeSoFar (list of positions), used to make the path
			this.currentPower += powerConsump;
			this.currentPos = pos;
			
			// at each step, distance-node map, distNodeMap is recaculated and sorted
			// this is done so that distances in distNodeMap are always up to date
			// and so that the drone can use any node within range
			Map<Double, Node> unsortedDistNodes = getUnsortedDistNodes();
			this.distNodeMap = sortDistNodes(unsortedDistNodes);
			
			// for every dist, node in distNodeMap...
			for (Entry<Double, Node> entry : this.distNodeMap.entrySet()) {
				double dist = entry.getKey();
				Node node = entry.getValue();
				// check if drone is in range of any nodes
				if (dist <= node.getRadius()) {
					// if so, use the node (green/red) and remove it from the list of nodes being considered
					use(node);
					this.mapNodes.removeIf(n -> n.used);
				}
				// we can break out of loop on the first iteration as the closest node will alwyas be the first entry
				// and if the first entry isn't in range then none of the others will be
				break;
			}
		}
		// next possible moves updated
		this.posChoices = getNextMoves();
		if (this.movesMadeSoFar.size() <= this.maxMovesAllowed) {
			this.movesMadeSoFar.add(pos);
			this.statsAtEachStep.add("p1,"+dirToMoveIn+",p2,"+this.currentCoins+","+this.currentPower);
		}
	}
}
