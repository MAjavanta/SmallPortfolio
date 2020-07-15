package uk.ac.ed.inf.powergrab;

public class Node {
	private static final double r = 0.00025; // radius of effect
	public boolean used = false;
	public double weight = 0;
	public double coins = 0;
	public double power = 0;
	public String type;
	public Position pos;
	
	// node (charging station) constructor
	public Node(float coins, float power, String type, Position pos) {
		this.coins = coins;
		this.power = power;
		this.type = type;
		this.pos = pos;
		this.weight = getWeight(); // "green-ness"/"red-ness" of a node
	}
	
	public String toString() {
		return "<coins:"+this.coins+", power:"+this.power+", type:"+this.type+", weight:"+
				this.weight+", used:"+this.used+", "+pos.toString()+")>";
	}

	// returns radius for effect (0.00025)
	public double getRadius() {
		return r;
	}

	// returns weight of a charging station, this is used to rank nodes
	public double getWeight() {
		// this is a function so that it can be changed later, to take current power and coins into account 
		return this.coins + this.power;
	}
}