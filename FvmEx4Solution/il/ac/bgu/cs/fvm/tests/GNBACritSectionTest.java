package il.ac.bgu.cs.fvm.tests;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import il.ac.bgu.cs.fvm.Exercise4FacadeImplementation;
import il.ac.bgu.cs.fvm.automata.Automaton;
import il.ac.bgu.cs.fvm.automata.MultiColorAutomaton;
import il.ac.bgu.cs.fvm.labels.State;

public class GNBACritSectionTest {

	@Test
	public void simpleTest() throws Exception {

		MultiColorAutomaton mulAut = getMCAut();
		Automaton aut = new Exercise4FacadeImplementation().GNBA2NBA(mulAut);
		System.out.println("my "+aut.getAcceptingStates());
		
		System.out.println("expec "+getExpected().getAcceptingStates());

		assertEquals(aut, getExpected());

	}

	MultiColorAutomaton getMCAut() {
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

	Automaton getExpected() {
		Automaton aut = new Automaton();
		State s01 = new State("0,1");
		State s11 = new State("1,1");
		State s21 = new State("2,1");
		State s02 = new State("0,2");
		State s12 = new State("1,2");
		State a22 = new State("2,2");

		HashSet<String> none = new HashSet<String>();
		HashSet<String> crit1 = new HashSet<String>(Arrays.asList("crit1"));
		HashSet<String> crit2 = new HashSet<String>(Arrays.asList("crit2"));
		HashSet<String> both = new HashSet<String>(Arrays.asList("crit2", "crit1"));

		aut.addTransition(s01, crit2, s21);
		aut.addTransition(s01, crit1, s11);
		aut.addTransition(s02, crit2, a22);
		aut.addTransition(s02, crit1, s12);

		// True transitions
		for (Set<String> s : asList(none, crit1, crit2, both)) {
			aut.addTransition(s01, s, s01);
			aut.addTransition(s11, s, s02);			
			aut.addTransition(s02, s, s02);
			aut.addTransition(s21, s, s01);
			aut.addTransition(s12, s, s02);
			aut.addTransition(a22, s, s01);
		}

		aut.setInitial(s01);

		aut.setAccepting(s11, 0);

		return aut;
	}

}
