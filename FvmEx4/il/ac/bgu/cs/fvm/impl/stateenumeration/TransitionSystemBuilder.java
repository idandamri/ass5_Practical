package il.ac.bgu.cs.fvm.impl.stateenumeration;

import java.util.HashSet;
import java.util.Set;

import il.ac.bgu.cs.fvm.Exercise4FacadeImplementation;
import il.ac.bgu.cs.fvm.labels.Action;
import il.ac.bgu.cs.fvm.labels.State;
import il.ac.bgu.cs.fvm.transitionsystem.Transition;
import il.ac.bgu.cs.fvm.transitionsystem.TransitionSystem;

public class TransitionSystemBuilder {

	static public TransitionSystem transitionSystemFromStateEnumerator(StateEnumerator se) {
		TransitionSystem ts = (new Exercise4FacadeImplementation()).createTransitionSystem();

		Set<EnumeratedState> statesSet = new HashSet<>(se.getInitialStates());

		// Add the initial states
		for (EnumeratedState s : statesSet) {
			State tsStateForCircuitState = new State(s.toString());
			ts.addState(tsStateForCircuitState);
			ts.addInitialState(tsStateForCircuitState);
		}

		// Traverse the state space and create the states and transitions
		Set<EnumeratedState> toAdd = new HashSet<>(statesSet);

		do {

			Set<EnumeratedState> lastAdded = new HashSet<>(toAdd);
			toAdd.clear();

			for (EnumeratedState s : lastAdded) {
				for (TransitionInput input : s.getPossibleInputs()) {

					EnumeratedState ns = s.nextState(input);
					if (!statesSet.contains(ns)) {
						ts.addState(new State(ns.toString()));
						toAdd.add(ns);
					}
					
					Action action = new Action(input.getName());

					ts.addAction(action);
					ts.addTransition(new Transition(new State(s.toString()), action, new State(ns.toString())));
				}
			}

			statesSet.addAll(toAdd);
		} while (!toAdd.isEmpty());

		// Create the labeling function.
		for (EnumeratedState s : statesSet) {
			for (String lbl : s.getLabels()) {
				ts.addAtomicProposition(lbl);
				ts.addLabel(new State(s.toString()), lbl);
			}
		}

		return ts;

	}

}
