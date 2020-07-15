package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// drone super class, contains all methods and attributes shared by both stateful and stateless
public class Drone {
	static final float powerConsump = (float) -1.25;
	public float currentPower;
	public float currentCoins;
	public int maxMovesAllowed = 250;
	public Position currentPos;
	public ArrayList<Position> posChoices;
	public ArrayList<Position> movesMadeSoFar = new ArrayList<Position>();
	public Random randomSeed = new Random();
	
	// used to output to txt file
	public ArrayList<Direction> dirsUsed = new ArrayList<Direction>();
	public ArrayList<String> statsAtEachStep = new ArrayList<String>(); 

	// drone constructor
	public Drone(Position initPos) {
		this.currentPos = initPos;
		this.currentPower = 250;
		this.currentCoins = 0;
		this.posChoices = getNextMoves(); 
		this.movesMadeSoFar.add(initPos);
	}
	
	// This method let's a drone use a charging station
	public void use(Node node) {
		// The drone can only use a charging station if it's "alive" and is within playable area
		if (isAlive() && this.currentPos.inPlayArea()) {
			// when the drone uses a station, the drone takes coins and power from station and it has been used
			this.currentPower += node.power;
			this.currentCoins += node.coins;
			
			node.coins = 0;
			node.power = 0;
			node.weight = node.getWeight(); // weight set to 0 (0+0)
			node.used = true;				// node marked as used, will no longer be considered
		}
	}
	
	// gets a list of (allowable) positions which correspond to moving in each direction
	public ArrayList<Position> getNextMoves() {
		// gets the list of all 16 directions and creates an empty postions arraylist
		List<Direction> allDirs = new Direction().getAllDirs();
		ArrayList<Position> posChoices = new ArrayList<Position>();
		
		// for each direction, dir
		for (Direction dir : allDirs) {
			// calculate position resulted by moving in that direction, nextPos
			Position nextPos = this.currentPos.nextPosition(dir);
			// check if nextPos is in playable area, if so add it to the list to be returned
			if (nextPos.inPlayArea()) {
				posChoices.add(nextPos);
			}
		}
		return posChoices;
	}
	
	
	// checks if the drone is "alive", has enough power to make another move
	public boolean isAlive() {
		// check against powerConsump not 0, or we'll get negative power
		return (this.currentPower > -powerConsump);
	}
	
	// prints the path to terminal
	// also for geo
	public String printPath() {
		
		
    	String coordinates = "";
    	for (int i=0; i<this.movesMadeSoFar.size(); i++) {
    		Position pos = this.movesMadeSoFar.get(i);
    		String coord = "          ["+pos.longitude+", "+pos.latitude+"]";
			
			if (i == this.movesMadeSoFar.size()-1) {
				coordinates += coord;
			} else {
				coordinates += coord+",";
			}
    	}
    	
    	return 	"    {" + 
    			"      \"type\": \"Feature\"," + 
    			"      \"properties\": { }," + 
    			"      \"geometry\": {" + 
    			"        \"type\": \"LineString\"," + 
    			"        \"coordinates\": [" + 
    			coordinates +
    			"        ]" + 
    			"      }" + 
    			"    }";	
	}
	
	public String toString() {
		String state = currentPower <= -powerConsump ? "DEAD" : "ALIVE";
		return "<state:"+state+", coins:"+this.currentCoins+", power:"
				+this.currentPower+", #moves:"+(this.movesMadeSoFar.size()-1)+", "+this.currentPos.toString()+">";
	}
	
	// Gets a random element out of an ArrayList<Position>, possibleNextPos
	public Position getRandom(ArrayList<Position> possibleNextPos) {
	    int rnd = randomSeed.nextInt(possibleNextPos.size());
	    return possibleNextPos.get(rnd);
	}
	
	// Gets a random element out of an ArrayList<Position>, possibleNextPos
	public Direction getRandomDirs(List<Direction> possibleDirs) {
	    int rnd = randomSeed.nextInt(possibleDirs.size());
	    return possibleDirs.get(rnd);
	}
}