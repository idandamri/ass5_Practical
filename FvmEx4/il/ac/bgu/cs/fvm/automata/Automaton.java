package il.ac.bgu.cs.fvm.automata;

import static java.util.Arrays.asList;

import java.util.Set;

import org.svvrl.goal.cmd.Constant;
import org.svvrl.goal.cmd.Context;
import org.svvrl.goal.cmd.EquivalenceCommand;
import org.svvrl.goal.cmd.Expression;
import org.svvrl.goal.cmd.LoadCommand;
import org.svvrl.goal.cmd.Lval;
import org.svvrl.goal.core.aut.fsa.FSA;
import org.svvrl.goal.core.aut.opt.RefinedSimulation;
import org.svvrl.goal.core.aut.opt.RefinedSimulation2;
import org.svvrl.goal.core.aut.opt.SimulationRepository;
import org.svvrl.goal.core.comp.ComplementRepository;
import org.svvrl.goal.core.comp.piterman.PitermanConstruction;
import org.svvrl.goal.core.io.CodecRepository;
import org.svvrl.goal.core.io.GFFCodec;

import il.ac.bgu.cs.fvm.goal.AutomatonIO;
import il.ac.bgu.cs.fvm.labels.State;

public class Automaton extends MultiColorAutomaton {

	public void setAccepting(State s) {
		super.setAccepting(s, 0);
	}

	public Set<State> getAcceptingStates() {
		return super.getAcceptingStates(0);
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof Automaton) {
			try {
				AutomatonIO.write((Automaton) other, "other.gff");
				AutomatonIO.write(this, "this.gff");

				Context context = new Context();

				Constant con1 = new Constant("this.gff");
				Constant con2 = new Constant("other.gff");

				Lval lval1 = new Lval("th", new Expression[] {});
				Lval lval2 = new Lval("ot", new Expression[] {});

				CodecRepository.add(0, new GFFCodec());

				SimulationRepository.addSimulation2("RefinedSimilarity", FSA.class, RefinedSimulation2.class);
				SimulationRepository.addSimulation("RefinedSimilarity", FSA.class, RefinedSimulation.class);

				ComplementRepository.add("Safra-Piterman Construction", PitermanConstruction.class);

				LoadCommand lc1 = new LoadCommand(asList(lval1, con1));
				lc1.eval(context);

				LoadCommand lc2 = new LoadCommand(asList(lval2, con2));
				lc2.eval(context);

				EquivalenceCommand ec = new EquivalenceCommand(asList(lval1, lval2));

				return (Boolean) ec.eval(context);

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return result;
	}
}
