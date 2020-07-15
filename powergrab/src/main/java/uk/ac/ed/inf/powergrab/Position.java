package uk.ac.ed.inf.powergrab;

public class Position {
	private static final double step = 0.0003; 	// magnitude r
	public double latitude; 					// y component
	public double longitude; 					// x component
	
	// position constructor 
	public Position(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	// returns a new position resulted by moving in a specific direction
	public Position nextPosition(Direction direction) {
		double angle = direction.angle;
		if (!inPlayArea()) { // if not in the playable area, move towards centre of map (avoids getting stuck)
			Position centreOfMap = new Position((55.942617+55.946233)/2, (-3.192473-3.184319)/2); //latitude then longitude for the center of our map
			angle = this.getAngleBetween(centreOfMap);
			angle = new Direction().snapDir(angle).angle;
		}
		// otherwise calculate and return new position
		double newLongitude = this.longitude + getStep()*Math.cos(angle);
		double newLatitude = this.latitude + getStep()*Math.sin(angle);
		return new Position(newLatitude, newLongitude);
	}
	
	// returns true or false depending on if a position is within the playable area
	public boolean inPlayArea() {
		// longitude in [−3.192473, −3.184319] and latitude in [55.942617, 55.946233]
		boolean withinLong = this.longitude > -3.192473 && this.longitude < -3.184319;	
		boolean withinLat = this.latitude > 55.942617 && this.latitude < 55.946233;
		return withinLong && withinLat;
	}
	
	// gets euclidean distance between current position and given position
	public double getL2Dist(Position pos) {
		// removing sqrt will speed up calculations but the comparisons values must be squared
		double deltaLat = Math.abs(this.latitude - pos.latitude);
		double deltaLong = Math.abs(this.longitude - pos.longitude);
		return Math.sqrt(Math.pow(deltaLat, 2) + Math.pow(deltaLong, 2)); //with sqrt
	}

	// getter to get step size (0.0003)
	public double getStep() {
		return step;
	}
	
	// returns true if current pos is the same as given pos
	// this func is needed for removing used nodes, so they are no longer considered
	public boolean isTheSame(Position pos) {
		boolean sameLat = this.latitude == pos.latitude;
		boolean sameLong = this.longitude == pos.longitude;
		return sameLat && sameLong;
	}
	
	public String toString() {
		return this.latitude+","+this.longitude;
	}
	
	// works out the angle between any two points from east, (x-axis) 
	public double getAngleBetween(Position nodePos) {
		// dx (change in x) and dy (change in y) can be used to calculate gradient, m
		// m = tan(a) = dy/dx, so a = atan(dy/dx)
		double dx = nodePos.longitude - this.longitude;
		dx = dx==0 ? 1 : dx; // if dx is 0, division by 0 will occur and cause problems
		double dy = nodePos.latitude - this.latitude;
		double m = dy/dx;
		double angle = Math.atan(m); 
		// CAST rules for tan(x)
		if (dx < 0) {
			angle = Math.PI + angle;
		} else if (dy < 0) {
			angle = 2*Math.PI + angle;
		}
		return angle; // in rads
	}
}
