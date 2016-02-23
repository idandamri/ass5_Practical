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

public class AutTsProductTests2 {
	Exercise4FacadeImplementation fvmFacadeImpl = new Exercise4FacadeImplementation();

	@Test
	public void autTimesTs() {
		TransitionSystem ts = buildTransitionSystem();
		Automaton aut = buildAutomaton();

		TransitionSystem comb = fvmFacadeImpl.product(ts, aut);

		TransitionSystem expected = expected();
		
		System.out.println(expected.getInitialStates());
		System.out.println(comb.getInitialStates());

		assertEquals(expected.getInitialStates(),      comb.getInitialStates());
		assertEquals(expected.getStates(),             comb.getStates());
		assertEquals(expected.getTransitions(),        comb.getTransitions());
		assertEquals(expected.getActions(),            comb.getActions());
		assertEquals(expected.getAtomicPropositions(), comb.getAtomicPropositions());
		assertEquals(expected.getLabelingFunction(),   comb.getLabelingFunction());
	}

	private Automaton buildAutomaton() {
		Automaton aut = new Automaton();

		State q0 = new State("q0");
		State q1 = new State("q1");
		State qf = new State("qF");

		HashSet<String> notRedAndNotYellow = new HashSet<String>();
		HashSet<String> redAndNotYellow = new HashSet<String>(asList("red"));
		HashSet<String> yellowAndNotRed = new HashSet<String>(asList("yellow"));
		HashSet<String> redAndYellow = new HashSet<String>(asList("red", "yellow"));

		aut.addTransition(q0, notRedAndNotYellow, q0);
		aut.addTransition(q0, yellowAndNotRed, q1);
		aut.addTransition(q0, redAndNotYellow, qf);
		aut.addTransition(q0, redAndYellow, qf);

		aut.addTransition(q1, yellowAndNotRed, q1);
		aut.addTransition(q1, redAndYellow, q1);
		aut.addTransition(q1, redAndNotYellow, q0);
		aut.addTransition(q1, notRedAndNotYellow, q0);

		aut.addTransition(qf, notRedAndNotYellow, qf);
		aut.addTransition(qf, redAndNotYellow, qf);
		aut.addTransition(qf, yellowAndNotRed, qf);
		aut.addTransition(qf, redAndYellow, qf);

		aut.setInitial(q0);
		aut.setAccepting(qf);
		return aut;
	}

	private TransitionSystem buildTransitionSystem() {
		TransitionSystem ts = fvmFacadeImpl.createTransitionSystem();

		State gr = new State("green");
		State yl = new State("yellow");
		State rd = new State("red");
		State ry = new State("red/yellow");

		ts.addState(gr);
		ts.addState(yl);
		ts.addState(rd);
		ts.addState(ry);

		ts.addInitialState(gr);

		Action sw = new Action("switch");
		ts.addAction(sw);

		ts.addTransition(new Transition(gr, sw, yl));
		ts.addTransition(new Transition(yl, sw, rd));
		ts.addTransition(new Transition(rd, sw, ry));
		ts.addTransition(new Transition(ry, sw, gr));

		ts.addAtomicProposition("yellow");
		ts.addAtomicProposition("red");

		ts.addLabel(yl, "yellow");
		ts.addLabel(rd, "red");
		return ts;
	}

	TransitionSystem expected() {
		TransitionSystem ts = fvmFacadeImpl.createTransitionSystem();

		State gr0 = new State("green,q0");
		State yl1 = new State("yellow,q1");
		State ry0 = new State("red/yellow,q0");
		State rd0 = new State("red,q0");

		ts.addState(gr0);
		ts.addState(yl1);
		ts.addState(ry0);
		ts.addState(rd0);

		ts.addInitialState(gr0);

		Action sw = new Action("switch");
		ts.addAction(sw);

		ts.addTransition(new Transition(rd0, sw, ry0));
		ts.addTransition(new Transition(yl1, sw, rd0));
		ts.addTransition(new Transition(gr0, sw, yl1));
		ts.addTransition(new Transition(ry0, sw, gr0));

		ts.addAtomicProposition("q1");
		ts.addAtomicProposition("q0");

		ts.addLabel(gr0, "q0");
		ts.addLabel(yl1, "q1");
		ts.addLabel(ry0, "q0");
		ts.addLabel(rd0, "q0");

		return ts;

	}

}