package uk.ac.ed.inf.powergrab;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

public class App 
{
    private static java.util.Scanner scanner;
    
	public static void main( String[] args ) throws IOException {
    	String day = args[0];
    	String month = args[1];
    	String year = args[2];
    	String lat = args[3];
    	String lon = args[4];
    	String seed = args[5];
    	String droneType = args[6];
		
		// Map is created via connecting to power grab inf website using a specific date
		// an initial position is chosen for the drone
		// a new state(less/ful) drone is created using the initial position.
		// The drone, named d, is made to play and it carries out the appropriate algorithm.
		// Path is then printed to terminal
    	
    	// checks if inputs are in the correct format (ISO) yyyy/mm/dd
    	month = month.length() == 1 ? "0"+month : month;
    	day = day.length() == 1 ? "0"+day : day;
    	String date = year+"/"+month+"/"+day;
    	// constructs the correct URL to use based on the date
    	String mapString = "http://homepages.inf.ed.ac.uk/stg/powergrab/"+date+"/powergrabmap.geojson";
    	
    	// establishes connection from power grab map for specific date and returns fc
    	FeatureCollection fc = connectToInfServer(mapString);    	
    	ArrayList<Node> mapNodes = getMapNodeList(fc);

    	Position initPos = new Position(Double.parseDouble(lat), Double.parseDouble(lon));

    	if (droneType.equalsIgnoreCase("stateless")) {
        	// The second argument of stateless drone is the seed for the random walk
        	// this is so that the random walk is reproducible
        	StatelessDrone d = new StatelessDrone(initPos, Long.parseLong(seed)); 
        	d.play(mapNodes);
        	printToFile(d, droneType, day, month, year, fc);

    	} else {
        	StatefulDrone d = new StatefulDrone(initPos);
        	d.play(mapNodes);
    		printToFile(d, droneType, day, month, year, fc);
    	}
    }
	
	// takes all parameters and prints to output files
	private static void printToFile(Drone d, String droneType, String day, String month, String year, FeatureCollection fc) throws FileNotFoundException, UnsupportedEncodingException {
    	ArrayList<String> linesOfFile = new ArrayList<String>();
    	for (int i=0; i<d.movesMadeSoFar.size()-1; i++) {
    		Position p1 = d.movesMadeSoFar.get(i);
    		Position p2 = d.movesMadeSoFar.get(i+1);
    		if (i >= d.statsAtEachStep.size()) {
    			break;
    		}
    		String newLine = d.statsAtEachStep.get(i).replace("p1", p1.toString()).replace("p2", p2.toString());
    		linesOfFile.add(newLine);
    	}
    	writeToFile(droneType+"-"+day+"-"+month+"-"+year+".txt", linesOfFile);
    	writeToFile(droneType+"-"+day+"-"+month+"-"+year+".geojson", fc.toJson().replace("}}]}", "}},"+d.printPath()+"]}"));
	}
	
	public static void writeToFile(String fileName, ArrayList<String> linesOfFile) {  
	        PrintWriter printWriter;  
	        try {    
	            printWriter = new PrintWriter(new FileOutputStream(fileName, true));
	            int length = linesOfFile.size();    
	            for(int i = 0; i < length; i++) {
	                printWriter.println(linesOfFile.get(i));  
	            }   
	            printWriter.close();    
	        }  
	        catch(IOException e) {  
	            System.out.println(e.getMessage());  
	        }  
	    }
	    
	public static void writeToFile(String fileName, String dPath) {  
	        PrintWriter printWriter;  
	        try {    
	            printWriter = new PrintWriter(new FileOutputStream(fileName, true));
                printWriter.println(dPath);  
	            printWriter.close();    
	        }  
	        catch(IOException e) {  
	            System.out.println(e.getMessage());  
	        }  
	    } 
    
	// returns an arraylist of node objects (charging stations)
    private static ArrayList<Node> getMapNodeList(FeatureCollection fc) throws IOException {
    	ArrayList<Node> mapNodes = new ArrayList<Node>();
    	for (Feature f : fc.features()) {
    		// fc's info is parsed (coins, power, type, location)
    		float coins = f.getProperty("coins").getAsFloat();
    		float power = f.getProperty("power").getAsFloat();
    		String type = f.getProperty("marker-symbol").toString();
    		Point p = (Point) f.geometry();
    		double lat = p.latitude();
    		double lon = p.longitude();
    		
    		// new node is created for each charging station and is added to an arraylist which is returned
    		Node n = ne02w Node(coins, power, type, new Position(lat, lon));
    		mapNodes.add(n);
    	}
    	return mapNodes;
    }
    
    // connects to given link and returns a feature collection
    private static FeatureCollection connectToInfServer(String mapString) throws IOException {
    	// creates the url using the mapString passed in and opens a connection
    	URL mapUrl = new URL(mapString);
    	HttpURLConnection conn = (HttpURLConnection) mapUrl.openConnection();
    	
    	// timeouts are set, http request set to GET and connection is started
    	conn.setReadTimeout(10000);
    	conn.setConnectTimeout(15000);
    	conn.setRequestMethod("GET");
    	conn.setDoInput(true);
    	conn.connect();
    	
    	// input stream returned by the connection is converted into a string (json)
    	// json is then converted into a feature collection and returned
    	String mapSource = convertStreamToString(conn.getInputStream());
    	return FeatureCollection.fromJson(mapSource);
    }
    
    // converts an input stream to a string so that it can be interpreted as a json for fc
    static String convertStreamToString(java.io.InputStream is) {
        scanner = new java.util.Scanner(is);
		java.util.Scanner s = scanner.useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}