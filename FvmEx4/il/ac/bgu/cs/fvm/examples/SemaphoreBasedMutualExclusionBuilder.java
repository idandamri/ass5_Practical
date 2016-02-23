package il.ac.bgu.cs.fvm.examples;

import static java.util.Arrays.asList;

import il.ac.bgu.cs.fvm.Exercise4FacadeImplementation;
import il.ac.bgu.cs.fvm.labels.Location;
import il.ac.bgu.cs.fvm.programgraph.PGTransition;
import il.ac.bgu.cs.fvm.programgraph.ProgramGraph;

public class SemaphoreBasedMutualExclusionBuilder {
	static Exercise4FacadeImplementation fvmFacadeImpl = new Exercise4FacadeImplementation();
	
	public static ProgramGraph build(int id) {
		ProgramGraph pg = fvmFacadeImpl.createProgramGraph();

		Location noncrit = new Location("noncrit" + id);
		Location wait = new Location("wait" + id);
		Location crit = new Location("crit" + id);

		pg.addLocation(noncrit);
		pg.addLocation(wait);
		pg.addLocation(crit);

		pg.addInitialLocation(noncrit);

		pg.addTransition(new PGTransition(noncrit, "true", "", wait));
		pg.addTransition(new PGTransition(wait, "y>0", "y:=y-1", crit));
		pg.addTransition(new PGTransition(crit, "true", "y:=y+1", noncrit));

		pg.addInitalization(asList("y:=1"));

		return pg;

	}

}
