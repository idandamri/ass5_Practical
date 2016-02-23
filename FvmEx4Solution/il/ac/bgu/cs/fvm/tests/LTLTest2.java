package il.ac.bgu.cs.fvm.tests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import il.ac.bgu.cs.fvm.Exercise4FacadeImplementation;
import il.ac.bgu.cs.fvm.automata.Automaton;
import il.ac.bgu.cs.fvm.labels.State;
import il.ac.bgu.cs.fvm.ltl.AtomicProposition;
import il.ac.bgu.cs.fvm.ltl.Ltl;
import il.ac.bgu.cs.fvm.ltl.Next;
import il.ac.bgu.cs.fvm.ltl.Not;
import il.ac.bgu.cs.fvm.ltl.Until;

public class LTLTest2 {

	Exercise4FacadeImplementation fvmFacadeImpl = new Exercise4FacadeImplementation();

	@Test
	public void test() {
		AtomicProposition p = new AtomicProposition("p");

		Ltl ltl = new Until(new Not(p), new Next(p));

		Automaton aut = fvmFacadeImpl.LTL2BA(ltl);
		System.out.println(aut.getAcceptingStates().size());
		System.out.println(expected().getAcceptingStates().size());
		
		
		System.out.println(aut.getTransitions());
		System.out.println(expected().getTransitions());

		assertEquals(aut, expected());

	}

	Automaton expected() {
		Automaton aut = new Automaton();
		aut.addTransition(new State("[(!p U ()p), !()p, !p],1"), new HashSet<String>(), new State("[(!p U ()p), !()p, !p],1"));
		aut.addTransition(new State("[(!p U ()p), !()p, !p],1"), new HashSet<String>(), new State("[(!p U ()p), !p, ()p],1"));
		aut.addTransition(new State("[(!p U ()p), !p, ()p],0"), new HashSet<String>(), new State("[(!p U ()p), ()p, p],1"));
		aut.addTransition(new State("[(!p U ()p), !p, ()p],0"), new HashSet<String>(), new State("[!(!p U ()p), !()p, p],1"));
		aut.addTransition(new State("[(!p U ()p), !p, ()p],1"), new HashSet<String>(), new State("[(!p U ()p), ()p, p],0"));
		aut.addTransition(new State("[(!p U ()p), !p, ()p],1"), new HashSet<String>(), new State("[!(!p U ()p), !()p, p],0"));
		aut.addTransition(new State("[(!p U ()p), ()p, p],1"), new HashSet<String>(Arrays.asList("p")), new State("[(!p U ()p), ()p, p],0"));
		aut.addTransition(new State("[(!p U ()p), ()p, p],1"), new HashSet<String>(Arrays.asList("p")), new State("[!(!p U ()p), !()p, p],0"));
		aut.addTransition(new State("[(!p U ()p), !()p, !p],0"), new HashSet<String>(), new State("[(!p U ()p), !()p, !p],1"));
		aut.addTransition(new State("[(!p U ()p), !()p, !p],0"), new HashSet<String>(), new State("[(!p U ()p), !p, ()p],1"));
		aut.addTransition(new State("[(!p U ()p), ()p, p],0"), new HashSet<String>(Arrays.asList("p")), new State("[(!p U ()p), ()p, p],1"));
		aut.addTransition(new State("[(!p U ()p), ()p, p],0"), new HashSet<String>(Arrays.asList("p")), new State("[!(!p U ()p), !()p, p],1"));
		aut.addTransition(new State("[!(!p U ()p), !()p, p],1"), new HashSet<String>(Arrays.asList("p")), new State("[(!p U ()p), !p, ()p],0"));
		aut.addTransition(new State("[!(!p U ()p), !()p, p],1"), new HashSet<String>(Arrays.asList("p")), new State("[(!p U ()p), !()p, !p],0"));
		aut.addTransition(new State("[!(!p U ()p), !()p, p],1"), new HashSet<String>(Arrays.asList("p")), new State("[!(!p U ()p), !()p, !p],0"));
		aut.addTransition(new State("[!(!p U ()p), !()p, p],0"), new HashSet<String>(Arrays.asList("p")), new State("[(!p U ()p), !()p, !p],1"));
		aut.addTransition(new State("[!(!p U ()p), !()p, p],0"), new HashSet<String>(Arrays.asList("p")), new State("[(!p U ()p), !p, ()p],1"));
		aut.addTransition(new State("[!(!p U ()p), !()p, p],0"), new HashSet<String>(Arrays.asList("p")), new State("[!(!p U ()p), !()p, !p],1"));
		aut.addTransition(new State("[!(!p U ()p), !()p, !p],0"), new HashSet<String>(), new State("[!(!p U ()p), !()p, !p],1"));
		aut.addTransition(new State("[!(!p U ()p), !()p, !p],1"), new HashSet<String>(), new State("[!(!p U ()p), !()p, !p],0"));
		aut.setInitial(new State("[(!p U ()p), !p, ()p],0"));
		aut.setInitial(new State("[(!p U ()p), !()p, !p],0"));
		aut.setInitial(new State("[(!p U ()p), ()p, p],0"));
		aut.setAccepting(new State("[(!p U ()p), !p, ()p],0"));
		aut.setAccepting(new State("[(!p U ()p), !()p, !p],0"));
		aut.setAccepting(new State("[(!p U ()p), ()p, p],0"));
		aut.setAccepting(new State("[!(!p U ()p), !()p, p],0"));
		aut.setAccepting(new State("[!(!p U ()p), !()p, !p],0"));
		return aut;
	}

}
