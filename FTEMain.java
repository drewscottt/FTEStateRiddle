/**
 * @author Drew Scott
 * 
 * Solution for FiveThirtyEight's Riddler: remove states from the lower 48 of the US (splitting MI, ignore DC)
 * 	to get two separate regions, then maximize the area of the smaller region.
 * 
 * Accomplished by: getting areas and neighbors for all states, then getting all subsets of size k (for k in 2 to 47)
 * 	to represent the removed states. Then, see if it results in 2 distinct regions. Then, wait until all k-removals
 * 	are completed, and find the one with the biggest smallest region.
 * 
 * Issues: subset generation is too much, out of memory on 6-removals (although, I'd be surprised if removing 6 states 
 * 	would be better than 5, due to losing too much area).
 */


import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;

public class FTEMain {
	
	public static void main(String [] args) {
		// areas and neighbor files
		File areaFile = new File("StateAreas.csv");
		File neighborFile = new File("NeighborStates.csv");
		
		// set up states list with the area and neigbor info
		ArrayList<State> states = createStatesAndAreas(areaFile);
		setNeighbors(states, neighborFile);
		
        // get total area
        float totalArea = 0;
        for (State state : states) {
            totalArea += (float) state.getArea();
        }
			
		// go through all possible removal combinations
		for (int i = 2; i < states.size() - 2; i++) {
			System.out.println("Size " + i + ": ");
			
			// get results for i-removal
			HashMap<ArrayList<Integer>, HashSet<State>> results = getResults(states, i);
			
			// parse the results, get the largestMin
			int largestMin = 0;
			int smallestMax = 0;
			HashSet<State> removed = null;
			for (ArrayList<Integer> result : results.keySet()) {
				// get the areas for this result
				largestMin = result.get(1);
				smallestMax = result.get(0);
				if (result.get(0) < result.get(1)) {
					largestMin = result.get(0);
					smallestMax = result.get(1);
				}

				removed = results.get(result);
			}


			// print the largestMin result, along with other interesting data
			System.out.println("Largest minimal area: " + largestMin + ", with " + smallestMax + " as the bigger chunk");
            
            float smallPercent = ((float) largestMin / totalArea) * 100;
            float bigPercent = ((float) smallestMax / totalArea) * 100;
            float removedPercent = ((float) (totalArea - (smallestMax + largestMin)) / totalArea) * 100;
            System.out.println("Percent areas: " + smallPercent + "%, " + bigPercent + "%, Removed: " + removedPercent + "%");
			
            float difference = smallestMax - largestMin;
			System.out.println("Difference: " + difference);
			
            float avg = (largestMin + smallestMax) / 2;
			float percentDiff = (difference / avg) * 100;
			System.out.println("% difference: " + percentDiff + "%");
			
            System.out.println("Removed states: " + removed);
		}
	}
	
	/**
	 * @param states: ArrayList of all of the states
	 * @param removals: the number of states to be removed
	 * 
	 * Returns a map of the result with the maximum area for the smallest of 2 chunks. 
	 * 
	 * @return results: a map, where keys are 2-length ArrayList<Integer> where each value is the area of distinct
	 * 	regions created by removing states. The values are sets of states that were removed to generate the area key.
	 */
	public static HashMap<ArrayList<Integer>, HashSet<State>> getResults(ArrayList<State> states, int removals) {
		HashMap<ArrayList<Integer>, HashSet<State>> results = new HashMap<ArrayList<Integer>, HashSet<State>>();

        ArrayList<State> removalCandidates = getRemovalCandidates(states);
		
		// get all of the removals-subsets for states
		ArrayList<HashSet<State>> allRemovals = getSubsets(removalCandidates, removals);
		
		// go through all removal-subsets
		for (HashSet<State> removal : allRemovals) {
			// copy states
			ArrayList<State> statesCopy = (ArrayList<State>) states.clone();
			
			// map to save neighbors for states, because we will be removing neighbors
			HashMap<State, ArrayList<State>> oldNeighbors = new HashMap<State, ArrayList<State>>();
			
			// remove the removal states from statesCopy and remove removals states from their neighbors
			for (State removedState : removal) {
				statesCopy.remove(removedState);
				
				for (State neighbor : removedState.getNeighbors()) {
					// save this neighbor's neighbors list, if haven't done so already
					if (!oldNeighbors.containsKey(neighbor)) {
						oldNeighbors.put(neighbor, (ArrayList<State>) neighbor.getNeighbors().clone());
					}
					
					neighbor.removeNeighbor(removedState);
				}
			}
			
			// set the ids for all of the states (i.e. 0 to statesCopy.size())
			setIds(statesCopy);
			
			// create the union of statesCopy
			UnionFind uf = makeUnionFind(statesCopy);
			
			// if union has 2 partitions, save it to results
			if (uf.getPartitions() == 2) {
				ArrayList<Integer> areas = uf.getNonZeroAreas();
                
                if (results.keySet().size() == 0) {
				    results.put(areas, removal);
                } else {
                    ArrayList<Integer> curBest = null;
                    for (ArrayList<Integer> key : results.keySet()) {
                        curBest = key;
                    }

                    int curMin = curBest.get(0);
                    int curMax = curBest.get(1);
                    if (curMin > curMax) {
                        curMin = curBest.get(1);
                        curMax = curBest.get(0);
                    }   

                    int thisMin = areas.get(0);
                    int thisMax = areas.get(1);
                    if (thisMin > thisMax) {
                        thisMin = areas.get(1);
                        thisMax = areas.get(0);
                    }

                    if (thisMin > curMin) {
                        results.remove(curBest);
                        results.put(areas, removal);
                    }
                }
			}
			
			// reset neighbors, since we deleted some
			for (State neighbor : oldNeighbors.keySet()) {
				neighbor.setNeighbors(oldNeighbors.get(neighbor));
			}
		}
		
		return results;
	}

    /**
    * @param states : list of all states
    * 
    * Returns a list of states with some removals. These removals are made based on my judgement, such that
    * I believe it is unlikely that removing any of these states will result in an optimal solution.
    * This greatly speeds up the process (enabled me to do 6 removal states, whereas before was only 5). 
    *
    * @return removalCandidates : list of all states, with several removals
    */	
    public static ArrayList<State> getRemovalCandidates(ArrayList<State> states) {
        ArrayList<State> removalCandidates = new ArrayList<State>(states);
        String[] leaveIn = new String[]{"ME", "NH", "VT", "MA", "CT", "NY", "NJ", "DE", "WA", "OR", "CA", "NV"};
        
        for (String s : leaveIn) {
            State state = getState(states, s);

            removalCandidates.remove(state);
        }

        return removalCandidates;
    }

	/**
	 * @param states: list of states
	 * 
	 * Unions all neighboring states
	 * 
	 * @return uf: UnionFind object, where all neighboring states have been union'ed
	 */
	public static UnionFind makeUnionFind(ArrayList<State> states) {
		UnionFind uf = new UnionFind(states);
		for (State state : states) {
			for (State neighbor : state.getNeighbors()) {
				uf.union(state, neighbor);
			}
		}
		
		return uf;
	}
	
	private static void getSubsets(ArrayList<State> states, int k, int idx, HashSet<State> current, ArrayList<HashSet<State>> solution) {
	    //successful stop clause
	    if (current.size() == k) {
	        solution.add(new HashSet<>(current));
	        return;
	    }
	    
	    //unseccessful stop clause
	    if (idx == states.size()) return;
	    
	    State x = states.get(idx);
	    current.add(x);
	    
	    //"guess" x is in the subset
	    try {
            getSubsets(states, k, idx+1, current, solution);
	    } catch (OutOfMemoryError e) {
            return;
        }
        current.remove(x);
	    
	    //"guess" x is not in the subset
        try {
            getSubsets(states, k, idx+1, current, solution);
	    } catch (OutOfMemoryError e) {
            return;
        }	    
	}

	public static ArrayList<HashSet<State>> getSubsets(ArrayList<State> states, int k) {
	    ArrayList<HashSet<State>> res = new ArrayList<HashSet<State>>();
	    getSubsets(states, k, 0, new HashSet<State>(), res);
	    return res;
	}
	
	/**
	 * @param states: list of states
	 * 
	 * sets ids for all states in states
	 */
	public static void setIds(ArrayList<State> states) {
		int count = 0;
		for (State state : states) {
			state.setId(count);
			count++;
		}
	}
	
	/**
	 * 
	 * @param states: list of states
	 * @param name: name of state being searched for
	 * 
	 * Searches states for a State with name name, returns if found, throws exception if not
	 * 
	 * @return state: State with name name
	 * @throws NoSuchElementException: if State with name name doesn't exist
	 */
	public static State getState(ArrayList<State> states, String name) throws NoSuchElementException {
		for (State curState : states) {
			if (curState.getName().equals(name)) return curState;
		}
		
		throw new NoSuchElementException("State with name " + name + "doesn't exist");
	}
	
	/**
	 * @param states: list of states
	 * @param neighborFile: file containing all neighbor pairs
	 * 
	 * Parses neighborFile and adds all neighbor pairings
	 */
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
	
	/**
	 * @param areaFile: file containingg area of each state
	 * 
	 * Parses areaFile and returns list of all states
	 * 
	 * @return states: list of all states
	 */
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
