/**
 * @author Drew Scott
 * 
 * Contains state class
 */

import java.util.ArrayList;


public class State {

	private String name;
	private int area;
	private ArrayList<State> neighbors;
	private int id;
	
	public State(String name) {
		this.name = name;
		this.neighbors = new ArrayList<State>();
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setArea(int area) {
		this.area = area;
	}
	
	public int getArea() {
		return this.area;
	}

	public void removeNeighbor(State state) {
		this.neighbors.remove(state);
	}
	
	public ArrayList<State> getNeighbors() {
		return this.neighbors;
	}
	
	public void addNeighbor(State state) {
		this.neighbors.add(state);
	}
	
	public void setNeighbors(ArrayList<State> newNeighbors) {
		this.neighbors = newNeighbors;
	}
	
	public String toString() {		
		return this.name;
	}
	
}
