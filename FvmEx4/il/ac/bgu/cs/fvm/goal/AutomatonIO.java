package il.ac.bgu.cs.fvm.goal;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import il.ac.bgu.cs.fvm.automata.Automaton;
import il.ac.bgu.cs.fvm.automata.MultiColorAutomaton;
import il.ac.bgu.cs.fvm.goal.GoalStructure.Acc;
import il.ac.bgu.cs.fvm.goal.GoalStructure.Alphabet;
import il.ac.bgu.cs.fvm.goal.GoalStructure.InitialStateSet;
import il.ac.bgu.cs.fvm.goal.GoalStructure.StateSet;
import il.ac.bgu.cs.fvm.goal.GoalStructure.TransitionSet;
import il.ac.bgu.cs.fvm.goal.GoalStructure.TransitionSet.Transition;
import il.ac.bgu.cs.fvm.labels.State;

public class AutomatonIO {

	public static void write(Automaton aut, String file) throws Exception {

		GoalStructure gs = new GoalStructure();

		gs.setLabelOn("Transition");
		gs.setType("FiniteStateAutomaton");

		gs.setAlphabet(new Alphabet());
		gs.alphabet.setType("Propositional");

		gs.setStateSet(new StateSet());

		gs.acc = new Acc();
		gs.acc.setType("Buchi");

		gs.initialStateSet = new InitialStateSet();

		gs.transitionSet = new TransitionSet();
		gs.transitionSet.setComplete("false");

		gs.stateSet.state = new NoDuplicatesList<>();
		gs.alphabet.proposition = new NoDuplicatesList<>();
		gs.acc.stateID = new NoDuplicatesList<>();
		gs.transitionSet.transition = new NoDuplicatesList<>();

		for (Entry<State, Map<Set<String>, Set<State>>> ent : aut.getTransitions().entrySet()) {

			for (Entry<Set<String>, Set<State>> tr : ent.getValue().entrySet()) {
				Set<String> symbol = tr.getKey();

				for (String s : symbol) {
					gs.alphabet.proposition.add(s);
				}
			}
		}

		long tid = 1;
		for (Entry<State, Map<Set<String>, Set<State>>> ent : aut.getTransitions().entrySet()) {
			State source = ent.getKey();

			il.ac.bgu.cs.fvm.goal.GoalStructure.StateSet.State stt = new il.ac.bgu.cs.fvm.goal.GoalStructure.StateSet.State();
			stt.setSid((long) source.getLabel().hashCode());
			stt.setLabel(source.getLabel());

			gs.stateSet.state.add(stt);

			for (Entry<Set<String>, Set<State>> tr : ent.getValue().entrySet()) {

				Set<String> symbol = tr.getKey();

				String label = "";
				for (String s : symbol) {
					label += s + " ";
				}

				for (String s : gs.alphabet.proposition) {
					if (!symbol.contains(s)) {
						label += "~" + s + " ";
					}
				}

				for (State destination : tr.getValue()) {
					stt = new il.ac.bgu.cs.fvm.goal.GoalStructure.StateSet.State();
					stt.setSid((long) destination.getLabel().hashCode());
					stt.setLabel(destination.getLabel());

					gs.stateSet.state.add(stt);

					// Transition
					Transition tran = new Transition();
					tran.setFrom((long) source.getLabel().hashCode());
					tran.setTo((long) destination.getLabel().hashCode());
					tran.label = label;
					tran.tid = tid++;
					gs.transitionSet.transition.add(tran);

					// If this is an initial state, copy the transition
					if (aut.getInitialStates().contains(source)) {
						Transition tran1 = new Transition();
						tran1.setFrom(0L);
						tran1.setTo((long) destination.getLabel().hashCode());
						tran1.label = label;
						tran1.tid = tid++;
						gs.transitionSet.transition.add(tran1);
					}

				}
			}
		}

		for (State s : aut.getAcceptingStates()) {
			gs.acc.stateID.add((long) s.hashCode());
		}

		// if (aut.getInitialStates().size() != 1)
		// throw new UnsupportedOperationException("Only automata with a single
		// initial state are supported");

		// Add a single initial state
		il.ac.bgu.cs.fvm.goal.GoalStructure.StateSet.State stt = new il.ac.bgu.cs.fvm.goal.GoalStructure.StateSet.State();
		stt = new il.ac.bgu.cs.fvm.goal.GoalStructure.StateSet.State();
		stt.setSid(0L);
		stt.setLabel("initial");
		gs.stateSet.state.add(stt);
		gs.initialStateSet.stateID = 0L;

		// for (State s : aut.getInitialStates()) {
		// gs.initialStateSet.stateID = (long) s.hashCode();
		// }

		JAXBContext jc = JAXBContext.newInstance("il.ac.bgu.cs.fvm.goal");
		Marshaller marshaller = jc.createMarshaller();
		marshaller.marshal(gs, new File(file));
	}

	public static MultiColorAutomaton read(String file) throws Exception {

		JAXBContext jc = JAXBContext.newInstance("il.ac.bgu.cs.fvm.goal");
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		GoalStructure gs = (GoalStructure) unmarshaller.unmarshal(new File(file));

		MultiColorAutomaton aut = new MultiColorAutomaton();

		for (Transition t : gs.getTransitionSet().getTransition()) {
			Set<String> symbol = new HashSet<String>(Arrays.asList(t.label.split(" ")));

			symbol = symbol.stream().filter(s -> !s.startsWith("~")).collect(Collectors.toSet());

			State source = new State("" + t.getFrom());
			State destination = new State("" + t.getTo());
			aut.addTransition(source, symbol, destination);

			if (gs.initialStateSet.getStateID() == t.getFrom()) {
				aut.setInitial(source);
			}

			if (gs.acc.stateID != null && gs.acc.stateID.contains(t.getFrom())) {
				aut.setAccepting(source, 0);
			}

			if (gs.initialStateSet.getStateID() == t.getTo()) {
				aut.setInitial(destination);
			}

			if (gs.acc.stateID != null && gs.acc.stateID.contains(t.getTo())) {
				aut.setAccepting(destination, 0);
			}
		}

		return aut;
	}

}

@SuppressWarnings("serial")
class NoDuplicatesList<E> extends LinkedList<E> {
	@Override
	public boolean add(E e) {
		if (this.contains(e)) {
			return false;
		} else {
			return super.add(e);
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> collection) {
		Collection<E> copy = new LinkedList<E>(collection);
		copy.removeAll(this);
		return super.addAll(copy);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> collection) {
		Collection<E> copy = new LinkedList<E>(collection);
		copy.removeAll(this);
		return super.addAll(index, copy);
	}

	@Override
	public void add(int index, E element) {
		if (this.contains(element)) {
			return;
		} else {
			super.add(index, element);
		}
	}
}