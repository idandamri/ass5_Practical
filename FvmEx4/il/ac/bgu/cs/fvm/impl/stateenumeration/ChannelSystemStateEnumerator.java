package il.ac.bgu.cs.fvm.impl.stateenumeration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import il.ac.bgu.cs.fvm.channelsystem.ChannelSystem;
import il.ac.bgu.cs.fvm.channelsystem.ParserBasedInterleavingActDef;
import il.ac.bgu.cs.fvm.labels.Location;
import il.ac.bgu.cs.fvm.programgraph.ActionDef;
import il.ac.bgu.cs.fvm.programgraph.ConditionDef;
import il.ac.bgu.cs.fvm.programgraph.PGTransition;
import il.ac.bgu.cs.fvm.programgraph.ProgramGraph;

/**
 * A class to facilitate the generation of logical circuit models.
 *
 */
public class ChannelSystemStateEnumerator implements StateEnumerator {

	protected ChannelSystem cs;

	protected Set<ActionDef>	actionDefs;
	protected Set<ConditionDef>	conditionDefs;

	Map<ProgramGraph,Map<Location, Set<PGTransition>>> outgoing = new HashMap<ProgramGraph,Map<Location, Set<PGTransition>>>();

	ParserBasedInterleavingActDef parserBasedInterleavingActDef = new ParserBasedInterleavingActDef();

	/**
	 * @param cs
	 * @param actionDefs2
	 * @param conditionDefs2
	 */
	public ChannelSystemStateEnumerator(ChannelSystem cs, Set<ActionDef> actionDefs, Set<ConditionDef> conditionDefs) {
		this.cs = cs;
		this.actionDefs = actionDefs;
		this.conditionDefs = conditionDefs;

		// Construct the outgoing map, for efficiency
		for (ProgramGraph pg : cs.getProgramGraphs()) {
			
			Map<Location, Set<PGTransition>> _outgoing = new HashMap<Location, Set<PGTransition>>();
			outgoing.put(pg, _outgoing);
			
			for (PGTransition tr : pg.getTransitions()) {
				
				Set<PGTransition> set = _outgoing.get(tr.getFrom());

				if (set == null) {
					set = new HashSet<PGTransition>();
					_outgoing.put(tr.getFrom(), set);
				}

				set.add(tr);

			}
		}

	}

	/**
	 * An object that represents a state of the program. Used for state
	 * enumeration.
	 *
	 */
	class ChannelSystemState implements EnumeratedState {

		/**
		 * An evaluation of all variables.
		 */
		Map<String, Object> eval;

		/**
		 * A location in the program graph.
		 */
		List<Location> locations;

		/**
		 * Construct a s program state.
		 * 
		 * @param location
		 *            A location in the program graph.
		 * @param eval
		 *            An evaluation of all variables.
		 */
		public ChannelSystemState(List<Location> locations, Map<String, Object> eval) {
			super();
			this.locations = locations;
			this.eval = eval;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fvm.StateInterface#getLabels()
		 */
		@Override
		public Set<String> getLabels() {
			Set<String> set = new HashSet<>();
			for (Map.Entry<String, Object> entry : eval.entrySet()) {
				set.add("" + entry.getKey() + " = " + entry.getValue());
			}
			return set;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fvm.StateInterface#getPossibleInputs()
		 */
		@Override
		public Set<TransitionInput> getPossibleInputs() {
			Set<TransitionInput> set = new HashSet<>();

			for (int i = 0; i < locations.size(); i++) {
				Location lc = locations.get(i);
				for (PGTransition t : outgoing.get(cs.getProgramGraphs().get(i)).get(lc)) { 
					if (parserBasedInterleavingActDef.isOneSidedAction(t.getAction())) {
						for (int j = i + 1; j < locations.size(); j++) {
							Location lc2 = locations.get(j);
							for (PGTransition t2 : outgoing.get(cs.getProgramGraphs().get(j)).get(lc2)) {
								if (parserBasedInterleavingActDef.isOneSidedAction(t.getAction())) {
									
									
									String cond1 = t.getCondition() == "" ? "true" : "("+t.getCondition()+")";
									String cond2 = t2.getCondition() == "" ? "true" : "("+t2.getCondition()+")";
									String condition = cond1 + "&&" + cond2;
									String action = t.getAction() + "|" + t2.getAction();

									if (parserBasedInterleavingActDef.isMatchingAction(action)) {

										boolean condSat = ConditionDef.evaluate(conditionDefs, eval, condition);

										Map<String, Object> nexteval = parserBasedInterleavingActDef.effect(eval, action);

										if (condSat && nexteval != null) {
											List<Location> nextlocations = new ArrayList<Location>(locations);
											nextlocations.set(i, t.getTo());
											nextlocations.set(j, t2.getTo());
											set.add(new CsTransitionInput(nextlocations, nexteval, action));
										}
									}
								}
							}

						}

					} else {

						boolean condSat = ConditionDef.evaluate(conditionDefs, eval, t.getCondition());
						Map<String, Object> nexteval = ActionDef.effect(actionDefs, eval, t.getAction());

						if (condSat && nexteval != null) {
							List<Location> nextlocations = new ArrayList<Location>(locations);
							nextlocations.set(i, t.getTo());
							set.add(new CsTransitionInput(nextlocations, nexteval, t.getAction()));
						}
					}
				}
			}

			return set;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fvm.StateInterface#nextState(java.lang.Object)
		 */
		@Override
		public EnumeratedState nextState(TransitionInput input) {
			CsTransitionInput in = (CsTransitionInput) input;
			return new ChannelSystemState(in.nextlocations, in.nexteval);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			Map<String, Object> e = new HashMap<String, Object>(eval);

			String locs = null;
			for (Location lc : locations) {
				if (locs == null)
					locs = lc.getLabel();
				else
					locs += "," + lc.getLabel();
			}

			return "[location=" + locs + ", eval=" + e + "]";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((eval == null) ? 0 : eval.hashCode());
			result = prime * result + ((locations == null) ? 0 : locations.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChannelSystemState other = (ChannelSystemState) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (eval == null) {
				if (other.eval != null)
					return false;
			} else if (!eval.equals(other.eval))
				return false;
			if (locations == null) {
				if (other.locations != null)
					return false;
			} else if (!locations.equals(other.locations))
				return false;
			return true;
		}

		private ChannelSystemStateEnumerator getOuterType() {
			return ChannelSystemStateEnumerator.this;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fvm.StateEnumerator#getInitialStates()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * il.ac.bgu.cs.fvm.programgraph.ProgramGraphInterface#getInitialStates()
	 */
	@Override
	public Set<EnumeratedState> getInitialStates() {

		Set<EnumeratedState> set = new HashSet<EnumeratedState>();

		Map<String, Object> eval = new HashMap<String, Object>();

		for (ProgramGraph pg : cs.getProgramGraphs()) {
			for (List<String> init : pg.getInitalizations()) {

				for (String s : init)
					eval = ActionDef.effect(actionDefs, eval, s);
			}
		}

		
		for (List<Location> locs : getInitLocs()) {
			set.add(new ChannelSystemState(locs, eval));
		}

		return set;
	}

	private Set<List<Location>> getInitLocs() {
		Set<List<Location>> ret = getInitLocs(cs.getProgramGraphs().size());		
		return ret;
	}

	private Set<List<Location>> getInitLocs(int recursion) {
		if (recursion == 0) {
			Set<List<Location>> ret = new HashSet<List<Location>>();
			ret.add(Arrays.asList());
			return ret;
		} else {
			Set<List<Location>> set = new HashSet<List<Location>>();
			for (List<Location> locations : getInitLocs(recursion - 1)) {
				for (Location lc : cs.getProgramGraphs().get(recursion - 1).getInitialLocations()) {
					List<Location> extendedLocations = new LinkedList<Location>(locations);
					extendedLocations.add(lc);
					set.add(extendedLocations);
				}
			}
			return set;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cs == null) ? 0 : cs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ChannelSystemStateEnumerator))
			return false;
		ChannelSystemStateEnumerator other = (ChannelSystemStateEnumerator) obj;
		if (cs == null) {
			if (other.cs != null)
				return false;
		} else if (!cs.equals(other.cs))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProgramGraphStateEnumerator [pg=" + cs + "]";
	}

}

class CsTransitionInput implements TransitionInput {

	List<Location>		nextlocations;
	Map<String, Object>	nexteval;
	String				action;

	public CsTransitionInput(List<Location> nextlocations, Map<String, Object> nexteval, String action) {
		this.nextlocations = nextlocations;
		this.nexteval = nexteval;
		this.action = action;
	}

	@Override
	public String getName() {
		return action;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CsTransitionInput [nextlocations=" + nextlocations + "]";
	}
	
}