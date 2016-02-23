package il.ac.bgu.cs.fvm.impl.stateenumeration;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import il.ac.bgu.cs.fvm.circuits.Circuit;

/**
 * A class to facilitate the generation of logical circuit models.
 *
 */
public class CircuitStateEnumerator implements StateEnumerator {

	protected Circuit circuit;

	/**
	 * Construct a circuit object.
	 * 
	 * @param nInputs
	 *            Number of input variables.
	 * @param nOutputs
	 *            Number of output variables.
	 * @param nRegisters
	 *            Number of registers.
	 */
	public CircuitStateEnumerator(Circuit circuit) {
		this.circuit = circuit;
	}

	/**
	 * A utility function that converts a decimal number to a vector of bits.
	 * 
	 * @param i
	 *            The number to convert
	 * @param nBits
	 *            The required length of the output vector.
	 * @return A vector of length nBits with the bits of the number.
	 */
	protected static List<Boolean> decToBin(int i, int nBits) {
		List<Boolean> output = new LinkedList<>();

		int mask = 1 << nBits - 1;

		while (mask > 0) {
			if ((mask & i) == 0) {
				output.add(false);
			} else {
				output.add(true);
			}
			mask = mask >> 1;
		}

		return output;
	}

	/**
	 * Generates a set of all initial states.
	 * 
	 * @see fvm.StateEnumerator#getInitialStates()
	 */
	@Override
	public Set<EnumeratedState> getInitialStates() {
		Set<EnumeratedState> s = new HashSet<>();

		int nInputCombinations = (1 << (circuit.getNumberOfInputPorts() + 1)) - 1;
		for (int i = 0; i < nInputCombinations; i++) {
			s.add(new State(this, decToBin(0, circuit.getNumberOfRegiters()), decToBin(i, circuit.getNumberOfInputPorts())));
		}

		return s;
	}
	/**
	 * A function that specifies how the registered are update at every tick of
	 * the clock.
	 * 
	 * @param registers
	 *            The current state of the registers.
	 * @param inputs
	 *            The current inputs to the circuit.
	 * @return The next state of the registers.
	 */
	// protected abstract List<Boolean> updateRegisters(List<Boolean> registers,
	// List<Boolean> inputs);

	/**
	 * A function that specifies the outputs of the circuit.
	 * 
	 * @param registers
	 *            The state the registers.
	 * @param inputs
	 *            The current input to the system.
	 * @return A vector representing the value of ll the outputs of the circuit.
	 */
	// protected abstract List<Boolean> computeOutputs(List<Boolean> registers,
	// List<Boolean> inputs);

	/**
	 * A class abstracting the state of a circuit.
	 *
	 */
	private class State implements EnumeratedState {

		private CircuitStateEnumerator parent;
		protected List<Boolean> registers;
		protected List<Boolean> inputs;

		/**
		 * Generates all the vectors of the input variables.
		 * 
		 * @see fvm.StateEnumerator#getPossibleInputs()
		 */
		@Override
		public Set<TransitionInput> getPossibleInputs() {
			Set<TransitionInput> s = new HashSet<>();

			int nInputCombinations = (1 << (parent.circuit.getNumberOfInputPorts() + 1)) - 1;

			for (int i = 0; i < nInputCombinations; i++) {
				s.add(new CiruitTransitionInput(decToBin(i, parent.circuit.getNumberOfInputPorts())));
			}

			return s;
		}

		/**
		 * Constructor of a state.
		 * 
		 * @param parent
		 *            The AbstractCircuit object that generated this state.
		 * @param registers
		 *            The state of the registers.
		 * @param inputs
		 *            The last inputs.
		 */
		public State(CircuitStateEnumerator parent, List<Boolean> registers, List<Boolean> inputs) {
			super();
			this.parent = parent;
			this.registers = registers;
			this.inputs = inputs;
		}

		/**
		 * Generate the next state by invoking the updateRegisters method.
		 * 
		 * @see fvm.StateInterface#nextState
		 */
		@Override
		public EnumeratedState nextState(TransitionInput ti) {
			return new State(parent, parent.circuit.updateRegisters(registers, inputs), ((CiruitTransitionInput) ti).v);
		}

		/**
		 * Compute the labels of the state by invoking the computeOutputs
		 * method.
		 * 
		 * @see fvm.StateInterface#getLabels()
		 */
		@Override
		public Set<String> getLabels() {
			Set<String> labels = new HashSet<>();

			int i = 0;
			for (Boolean b : registers) {
				i++;
				if (b)
					labels.add("r" + i);
			}

			i = 0;
			for (Boolean b : inputs) {
				i++;
				if (b)
					labels.add("x" + i);
			}

			i = 0;
			for (Boolean b : parent.circuit.computeOutputs(registers, inputs)) {
				i++;
				if (b)
					labels.add("y" + i);
			}

			return labels;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
			result = prime * result + ((parent == null) ? 0 : parent.hashCode());
			result = prime * result + ((registers == null) ? 0 : registers.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			State other = (State) obj;
			if (inputs == null) {
				if (other.inputs != null)
					return false;
			} else if (!inputs.equals(other.inputs))
				return false;
			if (parent == null) {
				if (other.parent != null)
					return false;
			} else if (!parent.equals(other.parent))
				return false;
			if (registers == null) {
				if (other.registers != null)
					return false;
			} else if (!registers.equals(other.registers))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "[registers=" + registers + ", inputs=" + inputs + "]";
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((circuit == null) ? 0 : circuit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CircuitStateEnumerator))
			return false;
		CircuitStateEnumerator other = (CircuitStateEnumerator) obj;
		if (circuit == null) {
			if (other.circuit != null)
				return false;
		} else if (!circuit.equals(other.circuit))
			return false;
		return true;
	}

}

class CiruitTransitionInput implements TransitionInput {

	List<Boolean> v;

	public CiruitTransitionInput(List<Boolean> v) {
		this.v = v;
	}

	@Override
	public String getName() {
		return "" + v;
	}

}