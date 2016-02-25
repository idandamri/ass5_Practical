package il.ac.bgu.cs.fvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import il.ac.bgu.cs.fvm.exceptions.DeletionOfAttachedActionException;
import il.ac.bgu.cs.fvm.exceptions.DeletionOfAttachedAtomicPropositionException;
import il.ac.bgu.cs.fvm.exceptions.DeletionOfAttachedStateException;
import il.ac.bgu.cs.fvm.exceptions.FVMException;
import il.ac.bgu.cs.fvm.exceptions.InvalidInitialStateException;
import il.ac.bgu.cs.fvm.exceptions.InvalidLablingPairException;
import il.ac.bgu.cs.fvm.exceptions.InvalidTransitionException;
import il.ac.bgu.cs.fvm.exceptions.TransitionSystemPart;
import il.ac.bgu.cs.fvm.labels.Action;
import il.ac.bgu.cs.fvm.labels.State;
import il.ac.bgu.cs.fvm.transitionsystem.Transition;
import il.ac.bgu.cs.fvm.transitionsystem.TransitionSystem;

public class TransitionSystemImp implements TransitionSystem {


	private String name;
	private Set <State> states;
	private Set <State> initialStates;
	private Set <Action> actions;
	private Set <String> atomicProposition;
	private Set <Transition> transitions;

	private Map<State, Set<String>> labelingFunction;

	public TransitionSystemImp() {
		this.states = new HashSet<State> ();
		this.initialStates = new HashSet<State> ();
		this.actions = new HashSet<Action> ();
		this.atomicProposition = new HashSet<String> ();
		this.labelingFunction = new HashMap<State, Set<String>>();
		this.transitions = new HashSet<Transition> ();

	}
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void addAction(Action action) {
		actions.add(action);
	}

	@Override
	public void addAtomicProposition(String p) {
		atomicProposition.add(p);
	}

	@Override
	public void addInitialState(State state) throws FVMException {
		if (states.contains(state))
			initialStates.add(state);
		else
			throw new InvalidInitialStateException(state);
	}

	@Override
	public void addLabel(State s, String l) throws FVMException {
		if (states.contains(s) && atomicProposition.contains(l)){
			Set <String> labelSet = null;
			if (labelingFunction.get(s)==null)
			{
				labelSet = new HashSet<String>();
				labelSet.add(l);
				labelingFunction.put(s, labelSet);
			}
			else
			{
				labelSet = labelingFunction.get(s);
				labelSet.add(l);
			}
		}
		else
			throw new InvalidLablingPairException(s, l);
	}

	@Override
	public void addState(State state) {
		states.add(state);
	}

	@Override
	public void addTransition(Transition t) throws FVMException {
		if (states.contains(t.getFrom()) &&
				states.contains(t.getTo()) &&
				actions.contains(t.getAction()))
			transitions.add(t);
		else
			throw new InvalidTransitionException(t);
	}

	@Override
	public Set<Action> getActions() {
		return actions;
	}

	@Override
	public Set<String> getAtomicPropositions() {
		return atomicProposition;
	}

	@Override
	public Set<State> getInitialStates() {
		return initialStates;
	}

	@Override
	public Map<State, Set<String>> getLabelingFunction() {
		return labelingFunction;
	}

	@Override
	public Set<State> getStates() {
		return states;
	}

	@Override
	public Set<Transition> getTransitions() {
		return transitions;
	}

	@Override
	public void removeAction(Action action) throws FVMException {
		if (!actionListedInTransition(action))
			actions.remove(action);
		else 
			throw new DeletionOfAttachedActionException(action, TransitionSystemPart.ACTIONS);
	}

	@Override
	public void removeAtomicProposition(String p) throws FVMException {
		for (Set <String> labels : labelingFunction.values())
		{
			if (labels.contains(p))
				throw new DeletionOfAttachedAtomicPropositionException(p, TransitionSystemPart.LABELING_FUNCTION);
		}
		atomicProposition.remove(p);
	}

	@Override
	public void removeInitialState(State state) {
		initialStates.remove(state);
	}

	@Override
	public void removeLabel(State s, String l) {
		Set <String> labelSet = labelingFunction.get(s);
		if (labelSet!=null)
		{
			labelSet.remove(l);
		}
	}

	@Override
	public void removeState(State state) throws FVMException {
		if (!initialStates.contains(state))
		{
			Set<String> g = labelingFunction.get(state);
			if (labelingFunction.get(state)==null ||labelingFunction.get(state).size()==0 )
			{
				if (!stateListedInTransition(state))
					states.remove(state);
				else
					throw new DeletionOfAttachedStateException(state, TransitionSystemPart.STATES);
			}
			else
				throw new DeletionOfAttachedStateException(state, TransitionSystemPart.STATES);
		}
		else
			throw new DeletionOfAttachedStateException(state, TransitionSystemPart.STATES);
	}


	@Override
	public void removeTransition(Transition t) {
		transitions.remove(t);
	}

	private boolean stateListedInTransition(State state) {
		for (Transition t : transitions)
		{
			if (t.getFrom().equals(state) || t.getTo().equals(state))
				return true;

		}
		return false;
	}

	private boolean actionListedInTransition(Action action) {
		for (Transition t : transitions)
		{
			if (t.getAction().equals(action) )
				return true;

		}
		return false;
	}
	
	private boolean equals(TransitionSystem otherTs)
	{
		if (otherTs.getInitialStates().equals(initialStates))
			if (otherTs.getStates().equals(states))
				if (otherTs.getActions().equals(actions))
					if (otherTs.getAtomicPropositions().equals(atomicProposition))
						if (otherTs.getLabelingFunction().equals(labelingFunction))	
							if (otherTs.getTransitions().equals(transitions))
								return true;
		return false;
	}

}
