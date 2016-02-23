package il.ac.bgu.cs.fvm.examples;

import static java.util.Arrays.asList;

import java.util.HashSet;

import il.ac.bgu.cs.fvm.automata.Automaton;
import il.ac.bgu.cs.fvm.labels.State;

public class RedGreenAutomatonBuilder {
	static public Automaton build() {
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
}
