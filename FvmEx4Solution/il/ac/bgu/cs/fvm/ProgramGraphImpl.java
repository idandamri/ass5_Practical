package il.ac.bgu.cs.fvm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import il.ac.bgu.cs.fvm.labels.Location;
import il.ac.bgu.cs.fvm.programgraph.PGTransition;
import il.ac.bgu.cs.fvm.programgraph.ProgramGraph;

public class ProgramGraphImpl implements ProgramGraph{

	private final String DEFAULT_NAME = "My Program Graph";
	private String name;
	private Set<List<String>> initializations = new HashSet<List<String>>();
	private Set<Location> locations = new HashSet<Location>();
	private Set<Location> initialLocations = new HashSet<Location>();
	private HashSet<PGTransition> transitions = new HashSet<PGTransition>();



	public ProgramGraphImpl() {
		this.name = DEFAULT_NAME;
	}

	@Override
	public void addInitalization(List<String> init) {
		this.initializations.add(init);	
	}

	@Override
	public void addInitialLocation(Location location) {
		if (locations.contains(location))
			initialLocations.add(location);
	}

	@Override
	public void addLocation(Location l) {
		locations.add(l);
	}

	@Override
	public void addTransition(PGTransition t) {
		transitions.add(t);
	}

	@Override
	public Set<List<String>> getInitalizations() {
		return this.initializations;
	}

	@Override
	public Set<Location> getInitialLocations() {
		return this.initialLocations;
	}

	@Override
	public Set<Location> getLocations() {
		return locations;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public HashSet<PGTransition> getTransitions() {
		return this.transitions;
	}

	public void removeLocation(String l) {
		locations.removeIf(loc -> loc.getLabel().equals(l));
	}

	@Override
	public void removeTransition(PGTransition t) {
		transitions.removeIf(trans -> trans.equals(t));
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void removeLocation(Location l) {
		locations.remove(l);		
	}



}
