import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;

public class FTEMain {
	
	public static void main(String [] args) {
		File areaFile = new File("StateAreas.csv");
		File neighborFile = new File("NeighborStates.csv");
		
		ArrayList<State> states = createStatesAndAreas(areaFile);
		setNeighbors(states, neighborFile);
		
		int area = 0;
		for (State state : states) {
			area += state.getArea();
		}
		
		System.out.println(area);
		
		for (int i = 2; i < states.size() - 2; i++) {
			System.out.println("Size " + i + ": ");
			
			HashMap<ArrayList<Integer>, HashSet<State>> results = getResults(states, i);
			
			int largestMin = 0;
			int smallestMax = 0;
			HashSet<State> removed = null;
			for (ArrayList<Integer> result : results.keySet()) {
				int min = 0;
				int max = 0;
				if (result.get(0) < result.get(1)) {
					min = result.get(0);
					max = result.get(1);
				} else {
					min = result.get(1);
					max = result.get(0);
				}

				if (min > largestMin) {
					removed = results.get(result);
					largestMin = min;
					smallestMax = max;
				}
			}
			
			System.out.println("Largest minimal area: " + largestMin + ", with " + smallestMax + " as the bigger chunk");
			float difference = smallestMax - largestMin;
			System.out.println("Difference: " + difference);
			float avg = (largestMin + smallestMax) / 2;
			float percentDiff = difference / avg;
			System.out.println("% difference: " + percentDiff);
			System.out.println("Removed states: " + removed);
		}
	}
	
	public static HashMap<ArrayList<Integer>, HashSet<State>> getResults(ArrayList<State> states, int removals) {
		HashMap<ArrayList<Integer>, HashSet<State>> results = new HashMap<ArrayList<Integer>, HashSet<State>>();
		
		ArrayList<HashSet<State>> allRemovals = getSubsets(states, removals);
		
		for (HashSet<State> removal : allRemovals) {
			// remove state
			ArrayList<State> statesCopy = (ArrayList<State>) states.clone();
			
			HashMap<State, ArrayList<State>> oldNeighbors = new HashMap<State, ArrayList<State>>();
			
			for (State removedState : removal) {
				statesCopy.remove(removedState);
				
				for (State neighbor : removedState.getNeighbors()) {
					if (!oldNeighbors.containsKey(neighbor)) {
						oldNeighbors.put(neighbor, (ArrayList<State>) neighbor.getNeighbors().clone());
					}
					
					neighbor.removeNeighbor(removedState);
				}
			}
			
			setIds(statesCopy);
			
			UnionFind uf = makeUnionFind(statesCopy);
			if (uf.getPartitions() == 2) {
				ArrayList<Integer> areas = uf.getNonZeroAreas();
				removal.add(uf.getParentOfBiggest());
				results.put(areas, removal);
			}
			
			for (State neighbor : oldNeighbors.keySet()) {
				neighbor.setNeighbors(oldNeighbors.get(neighbor));
			}
		}
		
		return results;
	}
	
	public static UnionFind makeUnionFind(ArrayList<State> states) {
		UnionFind uf = new UnionFind(states);
		for (State state : states) {
			for (State neighbor : state.getNeighbors()) {
				uf.union(state, neighbor);
			}
		}
		
		return uf;
	}
	
	private static void getSubsets(ArrayList<State> superSet, int k, int idx, HashSet<State> current, ArrayList<HashSet<State>> solution) {
	    //successful stop clause
	    if (current.size() == k) {
	        solution.add(new HashSet<>(current));
	        return;
	    }
	    
	    //unseccessful stop clause
	    if (idx == superSet.size()) return;
	    State x = superSet.get(idx);
	    current.add(x);
	    
	    //"guess" x is in the subset
	    getSubsets(superSet, k, idx+1, current, solution);
	    current.remove(x);
	    
	    //"guess" x is not in the subset
	    getSubsets(superSet, k, idx+1, current, solution);
	}

	public static ArrayList<HashSet<State>> getSubsets(ArrayList<State> superSet, int k) {
	    ArrayList<HashSet<State>> res = new ArrayList<>();
	    getSubsets(superSet, k, 0, new HashSet<State>(), res);
	    return res;
	}
	
	public static void setIds(ArrayList<State> states) {
		int count = 0;
		for (State state : states) {
			state.setId(count);
			count++;
		}
	}
	
	public static State getState(ArrayList<State> states, String name) throws NoSuchElementException {
		for (State curState : states) {
			if (curState.getName().equals(name)) return curState;
		}
		
		throw new NoSuchElementException("State with name " + name + "doesn't exist");
	}
	
	public static void setNeighbors(ArrayList<State> states, File neighborFile) {
		try {
			Scanner scanner = new Scanner(neighborFile);
			
			// first line is title line
			scanner.nextLine();
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				String firstName = line.substring(0, line.indexOf(","));
				String secondName = line.substring(line.indexOf(",") + 1);

				State firstState = getState(states, firstName);
				State secondState = getState(states, secondName);
				
				firstState.addNeighbor(secondState);
				secondState.addNeighbor(firstState);
			}
			
			scanner.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<State> createStatesAndAreas(File areaFile) {
		ArrayList<State> states = new ArrayList<State>();
		
		try {
			Scanner scanner = new Scanner(areaFile);
			
			// first line is title line
			scanner.nextLine();
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String name = line.substring(0, line.indexOf(","));
				int area = Integer.parseInt(line.substring(line.indexOf(",") + 1));
				
				State state = new State(name);
				state.setArea(area);
				
				states.add(state);
			}
			
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return states;
	}
	
}
