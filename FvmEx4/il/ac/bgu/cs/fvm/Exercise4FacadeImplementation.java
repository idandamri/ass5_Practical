package il.ac.bgu.cs.fvm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.RecognitionException;

import il.ac.bgu.cs.fvm.automata.Automaton;
import il.ac.bgu.cs.fvm.automata.MultiColorAutomaton;
import il.ac.bgu.cs.fvm.channelsystem.ChannelSystem;
import il.ac.bgu.cs.fvm.circuits.Circuit;
import il.ac.bgu.cs.fvm.exceptions.FVMException;
import il.ac.bgu.cs.fvm.labels.Action;
import il.ac.bgu.cs.fvm.labels.LabeledElement;
import il.ac.bgu.cs.fvm.labels.State;
import il.ac.bgu.cs.fvm.ltl.Ltl;
import il.ac.bgu.cs.fvm.programgraph.ActionDef;
import il.ac.bgu.cs.fvm.programgraph.ConditionDef;
import il.ac.bgu.cs.fvm.programgraph.ProgramGraph;
import il.ac.bgu.cs.fvm.transitionsystem.TransitionSystem;

public class Exercise4FacadeImplementation implements Exercise4Facade {
	@Override
	public ProgramGraph createProgramGraph() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TransitionSystem createTransitionSystem() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ProgramGraph interleave(ProgramGraph pg1, ProgramGraph pg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TransitionSystem interleave(TransitionSystem ts1, TransitionSystem ts2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TransitionSystem interleave(TransitionSystem ts1, TransitionSystem ts2, Set<Action> hs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isActionDeterministic(TransitionSystem ts) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAPDeterministic(TransitionSystem ts) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isExecution(TransitionSystem ts, List<LabeledElement> e) throws FVMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isExecutionFragment(TransitionSystem ts, List<LabeledElement> e) throws FVMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInitialExecutionFragment(TransitionSystem ts, List<LabeledElement> e) throws FVMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isMaximalExecutionFragment(TransitionSystem ts, List<LabeledElement> e) throws FVMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isStateTerminal(TransitionSystem ts, State s) throws FVMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<State> post(TransitionSystem ts, Set<State> c) throws FVMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<State> post(TransitionSystem ts, Set<State> c, Action a) throws FVMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<State> post(TransitionSystem ts, State s) throws FVMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<State> post(TransitionSystem ts, State s, Action a) throws FVMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<State> pre(TransitionSystem ts, Set<State> c) throws FVMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<State> pre(TransitionSystem ts, Set<State> c, Action a) throws FVMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<State> pre(TransitionSystem ts, State s) throws FVMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<State> pre(TransitionSystem ts, State s, Action a) throws FVMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ProgramGraph programGraphFromNanoPromela(String filename) throws RecognitionException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ProgramGraph programGraphFromNanoPromela(InputStream inputStream) throws RecognitionException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ProgramGraph programGraphFromNanoPromelaString(String nanopromela) throws RecognitionException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<State> reach(TransitionSystem ts) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TransitionSystem transitionSystemFromChannelSystem(ChannelSystem cs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TransitionSystem transitionSystemFromCircuit(Circuit c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TransitionSystem transitionSystemFromProgramGraph(ProgramGraph pg, Set<ActionDef> actionDefs, Set<ConditionDef> conditionDefs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TransitionSystem product(TransitionSystem ts, Automaton aut) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Automaton GNBA2NBA(MultiColorAutomaton mulAut) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Automaton LTL2BA(Ltl ltl) {
		throw new UnsupportedOperationException();
	}

}
