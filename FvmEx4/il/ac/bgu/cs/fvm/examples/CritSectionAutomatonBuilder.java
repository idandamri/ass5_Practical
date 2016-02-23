package il.ac.bgu.cs.fvm.examples;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

import il.ac.bgu.cs.fvm.automata.MultiColorAutomaton;
import il.ac.bgu.cs.fvm.labels.State;

public class CritSectionAutomatonBuilder {
	static public MultiColorAutomaton build() {
		MultiColorAutomaton aut = new MultiColorAutomaton();
		State s0 = new State("0");
		State s1 = new State("1");
		State s2 = new State("2");

		HashSet<String> none = new HashSet<String>();
		HashSet<String> crit1 = new HashSet<String>(asList("crit1"));
		HashSet<String> crit2 = new HashSet<String>(asList("crit2"));
		HashSet<String> both = new HashSet<String>(asList("crit1", "crit2"));

		aut.addTransition(s0, crit2, s2);
		aut.addTransition(s0, crit1, s1);

		// True transitions
		for (Set<String> s : asList(none, crit1, crit2, both)) {
			aut.addTransition(s0, new HashSet<String>(s), s0);
			aut.addTransition(s1, new HashSet<String>(s), s0);
			aut.addTransition(s2, new HashSet<String>(s), s0);
		}

		aut.setInitial(s0);
		aut.setAccepting(s1, 1);
		aut.setAccepting(s2, 2);

		return aut;
	}
}
