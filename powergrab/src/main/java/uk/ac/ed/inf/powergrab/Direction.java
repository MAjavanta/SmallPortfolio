package uk.ac.ed.inf.powergrab;

import java.util.Arrays;
import java.util.List;

public class Direction {
	public double angle;
	
	// direction constructor is overloaded so that methods can be used without defining an angle
	public Direction() {}
	
	public Direction(double angle) {
		this.angle = angle;
	}
	
	
	// "snaps" given angle to 1 of the 16 possible directions
	public Direction snapDir(double angle) {
		// get all possible directions
		// set the directionToMoveIn to the last value of the array 
		// this allows for comparisons with the last and first value of the array, (ESE and E)
		List<Direction> allDirs = getAllDirs();
		Direction directionToMoveIn = getAllDirs().get(getAllDirs().size()-1);
		
		// scan through allDirs and check if pairs of dirs fall in range and snap accordingly
		for (int i=0; i<allDirs.size()-1; i++) {
			Direction dir = allDirs.get(i);
			Direction nextDir = allDirs.get(i+1);
			
			// checks if angle falls in "lower" half, a <= x < (a+b)/2
			if (dir.angle <= angle && angle < (dir.angle+nextDir.angle)/2) {
				directionToMoveIn = dir; // snap to "lower" angle
				break;
			} 
			// checks if angle falls in "upper" half, (a+b)/2 <= x < b
			else if ((dir.angle+nextDir.angle)/2 <= angle && angle < nextDir.angle) {
				directionToMoveIn = nextDir; // snap to "upper" angle
				break;
			} 
			// last case checking between first and last entry of allDirs (E and ESE)
			// check has to be done because E=0 and ESE=337.5, and checking if E<=a<ESE won't work
			// so E=360 in this case (so check can work)
			// hard coded (not good), for ease of readability 
			else if (dir.angle == ESE.angle) {
				if ((dir.angle+2*Math.PI)/2 <= angle && angle < 2*Math.PI) {
					directionToMoveIn = dir;
				} else {
					directionToMoveIn = E;
				}
			}
		}
		
		return directionToMoveIn;
	} 
	
	// returns a list of all 16 directions so that scanning through possible dirs is made easier
	public List<Direction> getAllDirs() {
		List<Direction> allDirs = Arrays.asList(E, ENE, NE, NNE, N, NNW, NW, WNW, W, WSW, SW, SSW, S, SSE, SE, ESE);
		return allDirs;
	}
	
	// returns the correct direction based on the angle, should have used swithc statements
	public String toString() {
		if (this.angle == E.angle) {
			return "E";
		} if (this.angle == ENE.angle) {
			return "ENE";
		} if (this.angle == NE.angle) {
			return "NE";
		} if (this.angle == NNE.angle) {
			return "NNE";
		} if (this.angle == N.angle) {
			return "N";
		} if (this.angle == NNW.angle) {
			return "NNW";
		} if (this.angle == NW.angle) {
			return "NW";
		} if (this.angle == WNW.angle) {
			return "W";
		} if (this.angle == WSW.angle) {
			return "WSW";
		} if (this.angle == W.angle) {
			return "W";
		} if (this.angle == SW.angle) {
			return "SW";
		} if (this.angle == SSW.angle) {
			return "SSW";
		} if (this.angle == S.angle) {
		 	return "S";
		} if (this.angle == SSE.angle) {
			return "SSE";
		} if (this.angle == SE.angle) {
			return "SE";
		} else {
			return "ESE";
		}
	}
	
	public double getIncrement() {
		return (Direction.E.angle + Direction.ENE.angle)/2;
	}
	
	// east was chosen to be 0 degrees to avoid "CAST" calculations
	// angles in radians due to Math.cos(a) requires a to be in radians
	static final Direction E  = new Direction(0); 					// 0
	static final Direction ENE = new Direction(0.125 * Math.PI);	// 22.5
	static final Direction NE = new Direction(0.25 * Math.PI);		// 45
	static final Direction NNE = new Direction(0.375 * Math.PI);	// 67.5
	static final Direction N  = new Direction(0.5 * Math.PI);		// 90
	static final Direction NNW = new Direction(0.625 * Math.PI);	// 112.5
	static final Direction NW = new Direction(0.75 * Math.PI);		// 135
	static final Direction WNW = new Direction(0.875 * Math.PI);	// 157.5
	static final Direction W  = new Direction(Math.PI);				// 180
	static final Direction WSW = new Direction(1.125 * Math.PI);	// 202.5
	static final Direction SW = new Direction(1.25 * Math.PI);		// 225
	static final Direction SSW = new Direction(1.375 * Math.PI);	// 247.5
	static final Direction S  = new Direction(1.5 * Math.PI);		// 270
	static final Direction SSE = new Direction(1.625 * Math.PI);	// 292.5
	static final Direction SE = new Direction(1.75 * Math.PI);		// 315
	static final Direction ESE = new Direction(1.875 * Math.PI);	// 337.5
}