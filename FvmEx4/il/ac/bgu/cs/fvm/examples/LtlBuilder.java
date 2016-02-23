package il.ac.bgu.cs.fvm.examples;

import il.ac.bgu.cs.fvm.ltl.And;
import il.ac.bgu.cs.fvm.ltl.AtomicProposition;
import il.ac.bgu.cs.fvm.ltl.Ltl;
import il.ac.bgu.cs.fvm.ltl.Next;
import il.ac.bgu.cs.fvm.ltl.Not;
import il.ac.bgu.cs.fvm.ltl.Until;

public class LtlBuilder {
	static AtomicProposition p = new AtomicProposition("p");
	static AtomicProposition q = new AtomicProposition("q");
	static AtomicProposition s = new AtomicProposition("s");
	
	static Ltl build1() {
		return new And(new Not(p), new Next(p));
	}

	static Ltl build2() {
		return new Until(new Not(p), new Next(p));
	}

	static Ltl build3() {
		return new Until(p, new Until(q, s));
	}
}
