package il.ac.bgu.cs.fvm.tests;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.junit.Test;

import il.ac.bgu.cs.fvm.Exercise4FacadeImplementation;
import il.ac.bgu.cs.fvm.automata.Automaton;
import il.ac.bgu.cs.fvm.labels.Action;
import il.ac.bgu.cs.fvm.labels.State;
import il.ac.bgu.cs.fvm.transitionsystem.Transition;
import il.ac.bgu.cs.fvm.transitionsystem.TransitionSystem;

public class AutTsProductTests {
	Exercise4FacadeImplementation fvmFacadeImpl = new Exercise4FacadeImplementation();

	@Test
	public void autTimesTs() {
		TransitionSystem ts1 = buildTransitionSystem1();
		TransitionSystem ts2 = buildTransitionSystem2();
		Automaton aut = buildAutomaton();

		TransitionSystem comb1 = fvmFacadeImpl.product(ts1, aut);
		TransitionSystem expected1 = expected1();
		
		System.out.println(comb1.getInitialStates());
		System.out.println(expected1.getInitialStates());
			
		assertEquals(expected1.getInitialStates(), comb1.getInitialStates());
		assertEquals(expected1.getStates(), comb1.getStates());
		assertEquals(expected1.getTransitions(), comb1.getTransitions());
		assertEquals(expected1.getActions(), comb1.getActions());
		assertEquals(expected1.getAtomicPropositions(), comb1.getAtomicPropositions());
		assertEquals(expected1.getLabelingFunction(), comb1.getLabelingFunction());

		TransitionSystem comb2 = fvmFacadeImpl.product(ts2, aut);
		TransitionSystem expected2 = expected2();

		assertEquals(expected2.getInitialStates(), comb2.getInitialStates());
		assertEquals(expected2.getStates(), comb2.getStates());
		assertEquals(expected2.getTransitions(), comb2.getTransitions());
		assertEquals(expected2.getActions(), comb2.getActions());
		assertEquals(expected2.getAtomicPropositions(), comb2.getAtomicPropositions());
		assertEquals(expected2.getLabelingFunction(), comb2.getLabelingFunction());
	}

	private Automaton buildAutomaton() {
		Automaton aut = new Automaton();

		State q0 = new State("q0");
		State q1 = new State("q1");
		State q2 = new State("q2");

		HashSet<String> notRedAndNotGreen = new HashSet<String>();
		HashSet<String> redAndNotGreen = new HashSet<String>(asList("red"));
		HashSet<String> greenAndNotRed = new HashSet<String>(asList("green"));
		HashSet<String> redAndGreen = new HashSet<String>(asList("red", "green"));

		aut.addTransition(q0, notRedAndNotGreen, q0);
		aut.addTransition(q0, redAndNotGreen, q0);
		aut.addTransition(q0, greenAndNotRed, q0);
		aut.addTransition(q0, redAndGreen, q0);

		aut.addTransition(q0, notRedAndNotGreen, q1);
		aut.addTransition(q0, redAndNotGreen, q1);

		aut.addTransition(q1, notRedAndNotGreen, q1);
		aut.addTransition(q1, redAndNotGreen, q1);

		aut.addTransition(q1, greenAndNotRed, q2);
		aut.addTransition(q1, redAndGreen, q2);

		aut.addTransition(q2, notRedAndNotGreen, q2);
		aut.addTransition(q2, redAndNotGreen, q2);
		aut.addTransition(q2, greenAndNotRed, q2);
		aut.addTransition(q2, redAndGreen, q2);

		aut.setInitial(q0);
		aut.setAccepting(q1);
		return aut;
	}

	private TransitionSystem buildTransitionSystem1() {
		TransitionSystem ts = fvmFacadeImpl.createTransitionSystem();

		State rd = new State("red");
		State gr = new State("green");

		ts.addState(gr);
		ts.addState(rd);

		ts.addInitialState(rd);

		Action sw = new Action("switch");
		ts.addAction(sw);

		ts.addTransition(new Transition(rd, sw, gr));
		ts.addTransition(new Transition(gr, sw, rd));

		ts.addAtomicProposition("green");
		ts.addAtomicProposition("red");

		ts.addLabel(gr, "green");
		ts.addLabel(rd, "red");
		return ts;
	}

	private TransitionSystem buildTransitionSystem2() {
		TransitionSystem ts = fvmFacadeImpl.createTransitionSystem();

		State of = new State("off");
		State rd = new State("red");
		State gr = new State("green");

		ts.addState(of);
		ts.addState(gr);
		ts.addState(rd);

		ts.addInitialState(rd);

		Action sw = new Action("switch");
		ts.addAction(sw);

		ts.addTransition(new Transition(rd, sw, gr));
		ts.addTransition(new Transition(gr, sw, rd));
		ts.addTransition(new Transition(rd, sw, of));
		ts.addTransition(new Transition(of, sw, rd));

		ts.addAtomicProposition("green");
		ts.addAtomicProposition("red");

		ts.addLabel(gr, "green");
		ts.addLabel(rd, "red");
		return ts;
	}

	TransitionSystem expected1() {
		TransitionSystem ts = fvmFacadeImpl.createTransitionSystem();

		State gr2 = new State("green,q2");
		State gr0 = new State("green,q0");
		State rd2 = new State("red,q2");
		State rd1 = new State("red,q1");
		State rd0 = new State("red,q0");

		ts.addState(gr2);
		ts.addState(gr0);
		ts.addState(rd2);
		ts.addState(rd1);
		ts.addState(rd0);
		ts.addInitialState(rd1);
		ts.addInitialState(rd0);

		Action sw = new Action("switch");
		ts.addAction(sw);

		ts.addTransition(new Transition(gr0, sw, rd1));
		ts.addTransition(new Transition(gr2, sw, rd2));
		ts.addTransition(new Transition(gr0, sw, rd0));
		ts.addTransition(new Transition(rd0, sw, gr0));
		ts.addTransition(new Transition(rd2, sw, gr2));
		ts.addTransition(new Transition(rd1, sw, gr2));

		ts.addAtomicProposition("q1");
		ts.addAtomicProposition("q2");
		ts.addAtomicProposition("q0");

		ts.addLabel(gr2, "q2");
		ts.addLabel(gr0, "q0");
		ts.addLabel(rd2, "q2");
		ts.addLabel(rd1, "q1");
		ts.addLabel(rd0, "q0");

		return ts;

	}

	TransitionSystem expected2() {
		TransitionSystem ts = fvmFacadeImpl.createTransitionSystem();

		State gr2 = new State("green,q2");
		State gr0 = new State("green,q0");
		State of2 = new State("off,q2");
		State rd2 = new State("red,q2");
		State of0 = new State("off,q0");
		State of1 = new State("off,q1");
		State rd1 = new State("red,q1");
		State rd0 = new State("red,q0");

		ts.addState(gr2);
		ts.addState(gr0);
		ts.addState(of2);
		ts.addState(rd2);
		ts.addState(of0);
		ts.addState(of1);
		ts.addState(rd1);
		ts.addState(rd0);

		ts.addInitialState(rd1);
		ts.addInitialState(rd0);

		Action sw = new Action("switch");
		ts.addAction(sw);

		ts.addTransition(new Transition(rd1, sw, of1));
		ts.addTransition(new Transition(rd1, sw, gr2));
		ts.addTransition(new Transition(rd0, sw, of0));
		ts.addTransition(new Transition(rd0, sw, gr0));
		ts.addTransition(new Transition(rd0, sw, of1));
		ts.addTransition(new Transition(rd2, sw, of2));
		ts.addTransition(new Transition(rd2, sw, gr2));
		ts.addTransition(new Transition(gr0, sw, rd0));
		ts.addTransition(new Transition(gr0, sw, rd1));
		ts.addTransition(new Transition(of0, sw, rd1));
		ts.addTransition(new Transition(of0, sw, rd0));
		ts.addTransition(new Transition(gr2, sw, rd2));
		ts.addTransition(new Transition(of1, sw, rd1));
		ts.addTransition(new Transition(of2, sw, rd2));

		ts.addAtomicProposition("q1");
		ts.addAtomicProposition("q2");
		ts.addAtomicProposition("q0");

		ts.addLabel(gr2, "q2");
		ts.addLabel(gr0, "q0");
		ts.addLabel(of2, "q2");
		ts.addLabel(rd2, "q2");
		ts.addLabel(of0, "q0");
		ts.addLabel(of1, "q1");
		ts.addLabel(rd1, "q1");
		ts.addLabel(rd0, "q0");
		return ts;

	}

}