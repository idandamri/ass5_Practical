package il.ac.bgu.cs.fvm.tests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import il.ac.bgu.cs.fvm.Exercise4FacadeImplementation;
import il.ac.bgu.cs.fvm.automata.Automaton;
import il.ac.bgu.cs.fvm.labels.State;
import il.ac.bgu.cs.fvm.ltl.And;
import il.ac.bgu.cs.fvm.ltl.AtomicProposition;
import il.ac.bgu.cs.fvm.ltl.Ltl;
import il.ac.bgu.cs.fvm.ltl.Next;
import il.ac.bgu.cs.fvm.ltl.Not;

public class LTLTest {

	Exercise4FacadeImplementation fvmFacadeImpl = new Exercise4FacadeImplementation();

	@Test
	public void test() {
		AtomicProposition p = new AtomicProposition("p");

		Ltl ltl = new And(new Not(p), new Next(p));

		Automaton aut = fvmFacadeImpl.LTL2BA(ltl);
		//System.out.println("expec"+expected().getAcceptingStates());
		System.out.println(aut.getAcceptingStates().size());
		System.out.println(expected().getAcceptingStates().size());
		
		
		System.out.println(aut.getTransitions().size());
		System.out.println(expected().getTransitions().size());

		assertEquals(aut, expected());
	}

	Automaton expected() {
		Automaton aut = new Automaton();

		State p_np = new State("[!(!p /\\ ()p), ()p, p],0");
		State p_notnp = new State("[!(!p /\\ ()p), !()p, p],0");
		State notp_np = new State("[(!p /\\ ()p), !p, ()p],0");
		State notp_notnp = new State("[!(!p /\\ ()p), !()p, !p],0");

		HashSet<String> p = new HashSet<String>(Arrays.asList("p"));
		HashSet<String> notp = new HashSet<String>();

		aut.addTransition(p_np, p, p_np);
		aut.addTransition(p_notnp, p, notp_notnp);
		aut.addTransition(p_np, p, p_notnp);
		aut.addTransition(p_notnp, p, notp_np);
		aut.addTransition(notp_np, notp, p_np);
		aut.addTransition(notp_np, notp, p_notnp);
		aut.addTransition(notp_notnp, notp, notp_np);
		aut.addTransition(notp_notnp, notp, notp_notnp);

		aut.setInitial(notp_np);

		aut.setAccepting(p_np);
		aut.setAccepting(p_notnp);
		aut.setAccepting(notp_np);
		aut.setAccepting(notp_notnp);

		return aut;

	}
	

}
