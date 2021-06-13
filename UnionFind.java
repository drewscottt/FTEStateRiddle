/**
 * @author Drew Scott
 * 
 * Contains UnionFind class
 */

import java.util.ArrayList;

public class UnionFind {

	private ArrayList<State> states;
	
	private ArrayList<Integer> areas;
	private ArrayList<Integer> parents;
	private ArrayList<Integer> sizes;
	
	private int count;
	
	public UnionFind(ArrayList<State> states) {
		this.states = states;
		this.count = this.states.size();
		
		this.areas = new ArrayList<Integer>();
		this.parents = new ArrayList<Integer>();
		this.sizes = new ArrayList<Integer>();
		
		for (int i = 0; i < this.count; i++) {
			this.areas.add(this.states.get(i).getArea());
			
			this.parents.add(this.states.get(i).getId());
			
			this.sizes.add(1);
		}
	}
	
	/**
	 * adds state1 and state2 to the same union
	 * 
	 * @param state1
	 * @param state2
	 */
	public void union(State state1, State state2) {
		int id1 = state1.getId();
		int id2 = state2.getId();
		
		int parent1 = this.find(id1);
		int parent2 = this.find(id2);
		
		// return if both states in same union
		if (parent1 == parent2) return;
		
		// calculate new size and area for this union
		int newSize = this.sizes.get(parent1) + this.sizes.get(parent2);
		int newArea = this.areas.get(parent1) + this.areas.get(parent2);
		
		// determine which union is bigger and which is smaller
		int bigger = parent1; 
		int smaller = parent2;
		if (this.sizes.get(parent1) < this.sizes.get(parent2)) {
			bigger = parent2;
			smaller = parent1;
		}
		
		// update the bigger union to include the smaller one
		// zero out the smaller one's size and areas
		this.sizes.set(bigger, newSize);
		this.sizes.set(smaller, 0);
		
		this.areas.set(bigger, newArea);
		this.areas.set(smaller, 0);
		
		this.parents.set(smaller, bigger);
	}
	
	/**
	 * @param id
	 * @return the parent id of the input id
	 */
	public int find(int id) {
		int parent = this.parents.get(id);
		while (id != parent) {
			id = parent;
			
			parent = this.parents.get(id);
		}
		
		return parent;
	}
	
	/** 
	 * @return the number of disjoint partitions
	 */
	public int getPartitions() {
    	int parts = 0;
    	for (int i = 0; i < this.count; i++) {
    		if (this.sizes.get(i) != 0) parts++;
    	}
    	
    	return parts;
    }
	
	/**
	 * @return list of the areas of all of the nonzero areas
	 */
	public ArrayList<Integer> getNonZeroAreas() {
		ArrayList<Integer> nonZeroAreas = new ArrayList<Integer>();
		
		for (int area : this.areas) {
			if (area != 0) nonZeroAreas.add(area);
		}
		
		return nonZeroAreas;
	}
	
	/**
	 * @return the parent State of the union with the biggest area
	 */
	public State getParentOfBiggest() {
		int indexOfMax = 0;
		int max = 0;
		
		int count = 0;
		for (int area : this.areas) {
			if (area > max) {
				max = area;
				indexOfMax = count;
			}
			count++;
		}
		
		State parent = this.states.get(indexOfMax);
				
		return parent;
	}
}
