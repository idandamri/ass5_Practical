package il.ac.bgu.cs.fvm.automata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import il.ac.bgu.cs.fvm.labels.State;

public class MultiColorAutomaton {

	private Set<State>									initial;
	private Map<Integer, Set<State>>					accepting;
	private Map<State, Map<Set<String>, Set<State>>>	transitions;

	public MultiColorAutomaton() {
		transitions = new HashMap<State, Map<Set<String>, Set<State>>>();
		initial = new HashSet<State>();
		accepting = new HashMap<Integer, Set<State>>();
	}

	public void addState(State s) {
		if (!transitions.containsKey(s))
			transitions.put(s, new HashMap<Set<String>, Set<State>>());
	}

	public void addTransition(State source, Set<String> symbol, State destination) {
		if (!transitions.containsKey(source))
			addState(source);

		if (!transitions.containsKey(destination))
			addState(destination);

		Set<State> set = transitions.get(source).get(symbol);
		if (set == null) {
			set = new HashSet<State>();
			transitions.get(source).put(symbol, set);
		}
		set.add(destination);
	}

	public Set<State> getAcceptingStates(int color) {
		Set<State> acc = accepting.get(color);

		if (acc == null) {
			acc = new HashSet<State>();
			accepting.put(color, acc);
		}

		return acc;
	}

	public Set<State> getInitialStates() {
		return initial;
	}

	public Map<State, Map<Set<String>, Set<State>>> getTransitions() {
		return transitions;
	}

	public Iterable<State> nextStates(State source, Set<String> symbol) {
		if (!transitions.containsKey(source))
			throw new IllegalArgumentException();
		else
			return transitions.get(source).get(symbol);
	}

	public void setAccepting(State s, int color) {
		Set<State> acc = accepting.get(color);

		if (acc == null) {
			acc = new HashSet<State>();
			accepting.put(color, acc);
		}

		addState(s);
		acc.add(s);
	}

	public void setInitial(State s) {
		addState(s);
		initial.add(s);
	}

	public Set<Integer> getColors() {
		return accepting.keySet();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accepting == null) ? 0 : accepting.hashCode());
		result = prime * result + ((initial == null) ? 0 : initial.hashCode());
		result = prime * result + ((transitions == null) ? 0 : transitions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MultiColorAutomaton other = (MultiColorAutomaton) obj;
		if (accepting == null) {
			if (other.accepting != null)
				return false;
		} else if (!accepting.equals(other.accepting))
			return false;
		if (initial == null) {
			if (other.initial != null)
				return false;
		} else if (!initial.equals(other.initial))
			return false;
		if (transitions == null) {
			if (other.transitions != null)
				return false;
		} else if (!transitions.equals(other.transitions))
			return false;
		return true;
	}
	
	
	
}


