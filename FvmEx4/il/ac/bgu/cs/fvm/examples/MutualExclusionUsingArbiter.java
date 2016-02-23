package il.ac.bgu.cs.fvm.examples;

import il.ac.bgu.cs.fvm.Exercise4FacadeImplementation;
import il.ac.bgu.cs.fvm.labels.Action;
import il.ac.bgu.cs.fvm.labels.State;
import il.ac.bgu.cs.fvm.transitionsystem.Transition;
import il.ac.bgu.cs.fvm.transitionsystem.TransitionSystem;

// Figure 2.12 in the book
public class MutualExclusionUsingArbiter {

	static Exercise4FacadeImplementation fvmFacadeImpl = new Exercise4FacadeImplementation();
	
	static public TransitionSystem buildP() {
		TransitionSystem ts = fvmFacadeImpl.createTransitionSystem();

		State nc = new State("noncrit");
		State cr = new State("crit");

		ts.addState(nc);
		ts.addState(cr);

		ts.addInitialState(nc);

		Action req = new Action("request");
		Action rel = new Action("release");

		ts.addAction(req);
		ts.addAction(rel);

		ts.addTransition(new Transition(nc, req, cr));
		ts.addTransition(new Transition(cr, rel, nc));

		return ts;
	}

	static public TransitionSystem buildArbiter() {
		TransitionSystem ts = fvmFacadeImpl.createTransitionSystem();

		State unlock = new State("unlock");
		State lock = new State("lock");

		ts.addState(unlock);
		ts.addState(lock);

		ts.addInitialState(unlock);

		Action req = new Action("request");
		Action rel = new Action("release");

		ts.addAction(req);
		ts.addAction(rel);

		ts.addTransition(new Transition(unlock, req, lock));
		ts.addTransition(new Transition(lock, rel, unlock));

		return ts;
	}

}
