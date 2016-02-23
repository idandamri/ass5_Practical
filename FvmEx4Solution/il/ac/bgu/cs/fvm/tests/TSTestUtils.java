package il.ac.bgu.cs.fvm.tests;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import il.ac.bgu.cs.fvm.Exercise4FacadeImplementation;
import il.ac.bgu.cs.fvm.labels.Action;
import il.ac.bgu.cs.fvm.labels.LabeledElement;
import il.ac.bgu.cs.fvm.labels.State;
import il.ac.bgu.cs.fvm.transitionsystem.Transition;
import il.ac.bgu.cs.fvm.transitionsystem.TransitionSystem;

/**
 * Helps building transition systems. Contains some sample transition systems
 * for us to work on.
 * 
 * @author michael
 */
public class TSTestUtils {

	private final Map<String, State>	states	= new HashMap<>();
	private final Map<String, Action>	actions	= new HashMap<>();

	public State state(String name) {
		return states.computeIfAbsent(name, n -> new State(n));
	}

	public Action action(String name) {
		return actions.computeIfAbsent(name, n -> new Action(n));
	}

	public Transition transition(State f, Action a, State t) {
		return new Transition(f, a, t);
	}

	public Transition transition(String f, String a, String t) {
		return new Transition(state(f), action(a), state(t));
	}

	public Set<State> states(String... stateList) {
		return Arrays.stream(stateList).map(s -> state(s)).collect(Collectors.toSet());
	}

	public List<LabeledElement> trace(String... stateActionState) {
		List<Function<String, LabeledElement>> mappers = new ArrayList<Function<String, LabeledElement>>(2);
		mappers.add(s -> action(s));
		mappers.add(s -> state(s));
		AtomicInteger i = new AtomicInteger(0);
		return Arrays.stream(stateActionState).map(s -> mappers.get(i.incrementAndGet() % 2).apply(s)).collect(Collectors.toList());
	}

	public TransitionSystem addTagsByStateNames(TransitionSystem ts) {

		ts.getStates().forEach(s -> {
			ts.addAtomicProposition(s.getLabel());
			ts.addLabel(s, s.getLabel());
		});

		return ts;
	}

	/**
	 * {@code
	 *    +----------------- delta ------------------+
	 *    |                                          | 
	 *    v                                          | 
	 *  ((a))--alpha--> (b) --beta--> (c) --gamma-> (d)
	 *   
	 * }
	 * 
	 * 
	 * @return a simple transition system.
	 */
	public TransitionSystem simpleTransitionSystem() {
		TransitionSystem ts = new Exercise4FacadeImplementation().createTransitionSystem();

		ts.setName("Simple Transition System");

		ts.addState(state("a"));
		ts.addState(state("b"));
		ts.addState(state("c"));
		ts.addState(state("d"));

		ts.addAction(action("alpha"));
		ts.addAction(action("beta"));
		ts.addAction(action("gamma"));
		ts.addAction(action("delta"));

		ts.addAtomicProposition("System stable");
		ts.addAtomicProposition("System unstable");
		ts.addAtomicProposition("System stable-ish");

		ts.addInitialState(state("a"));

		ts.addTransition(transition("a", "alpha", "b"));
		ts.addTransition(transition("b", "beta", "c"));
		ts.addTransition(transition("c", "gamma", "d"));
		ts.addTransition(transition("d", "delta", "a"));

		ts.addLabel(state("a"), "System stable");
		ts.addLabel(state("b"), "System unstable");
		ts.addLabel(state("c"), "System stable-ish");
		ts.addLabel(state("d"), "System stable-ish");

		return ts;
	}

	/**
	 * Creates a linear transition system, with the states starting from
	 * {@code s1} to {@code s<num>} and the actions going from {@code a1} to
	 * {@code a<num-1>}.
	 * 
	 * @param stateNum
	 *            number of states in the generated system.
	 * @param statePrefix
	 * @param actionPrefix
	 * @return A linear transition system.
	 */
	public TransitionSystem makeLinearTs(int stateNum, String statePrefix, String actionPrefix) {
		TransitionSystem retVal = new Exercise4FacadeImplementation().createTransitionSystem();
		IntStream.rangeClosed(1, stateNum).mapToObj(i -> state(statePrefix + i)).forEach(s -> retVal.addState(s));
		IntStream.rangeClosed(1, stateNum - 1).mapToObj(i -> action(actionPrefix + i)).forEach(a -> retVal.addAction(a));
		IntStream.rangeClosed(1, stateNum - 1).forEach(i -> retVal.addTransition(transition(state(statePrefix + i), action(actionPrefix + i), state(statePrefix + (i + 1)))));

		retVal.addInitialState(state("s" + 1));

		return retVal;
	}

	public TransitionSystem makeLinearTs(int stateNum) {
		return makeLinearTs(stateNum, "s", "a");
	}

	/**
	 * Creates a circular transition system, with the states starting from
	 * {@code s1} to {@code s<num>} and the actions going from {@code a1} to
	 * {@code a<num>}.
	 * 
	 * @param stateNum
	 *            number of states in the generated system.
	 * @return A circular transition system.
	 */
	public TransitionSystem makeCircularTs(int stateNum) {
		TransitionSystem retVal = makeLinearTs(stateNum);
		retVal.addAction(action("a" + stateNum));
		retVal.addTransition(transition(state("s" + stateNum), action("a" + stateNum), state("s" + 1)));

		return retVal;
	}

	/**
	 * Creates a circular transition system, with the states starting from
	 * {@code s1} to {@code s<num>} and the actions going from {@code a1} to
	 * {@code a<num>}, and a reset action that goes to {@code s1} from every
	 * state.
	 * 
	 * @param stateNum
	 *            number of states in the generated system.
	 * @return A circular transition system with a "reset" action.
	 */
	public TransitionSystem makeCircularTsWithReset(int stateNum) {
		TransitionSystem retVal = makeCircularTs(stateNum);
		retVal.addAction(action("a" + stateNum));
		retVal.addTransition(transition(state("s" + stateNum), action("a" + stateNum), state("s" + 1)));
		Action reset = action("reset");
		retVal.addAction(reset);
		IntStream.rangeClosed(1, stateNum).forEach(i -> retVal.addTransition(transition(state("s" + i), reset, state("s1"))));

		return retVal;
	}

	/**
	 * Creates a transition system that has an indeterministic 3-fork at
	 * {@code statenum-3}. Branch states are {@code s_<b>_<i>}, where {@code b}
	 * is the branch number. Indeterministic action is {@code fork}.
	 * 
	 * @param stateNum
	 * @param branchCount
	 * @return
	 */
	public TransitionSystem makeBranchingTs(int stateNum, int branchCount) {
		TransitionSystem retVal = makeLinearTs(stateNum);

		int branchPoint = stateNum - 3;
		retVal.addAction(action("fork"));
		IntStream.rangeClosed(1, branchCount).forEach(branchNum -> {
			String statePrefix = "s_" + branchNum + "_";
			IntStream.rangeClosed(branchPoint + 1, stateNum).mapToObj(i -> state(statePrefix + i)).forEach(s -> retVal.addState(s));
			IntStream.rangeClosed(branchPoint + 1, stateNum - 1).forEach(i -> retVal.addTransition(transition(state(statePrefix + i), action("a" + i), state(statePrefix + (i + 1)))));
			retVal.addTransition(transition(state("s" + branchPoint), action("fork"), state(statePrefix + (branchPoint + 1))));
		});

		return retVal;
	}

	public static String prettyPrintXml(String xml) {
		try {
			final InputSource src = new InputSource(new StringReader(xml));
			final Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
			final Boolean keepDeclaration = xml.startsWith("<?xml");

			final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
			final LSSerializer writer = impl.createLSSerializer();

			writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
			writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);

			return writer.writeToString(document);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
