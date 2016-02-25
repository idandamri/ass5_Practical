package il.ac.bgu.cs.fvm;

import il.ac.bgu.cs.fvm.labels.State;

import java.util.List;

public class CircuitStateData {

	private List<Boolean> inputs;
	private List<Boolean> registers;
	private State state;
	public CircuitStateData(List<Boolean> inputs, List<Boolean> registers,
			State state) {
		this.inputs = inputs;
		this.registers = registers;
		this.state = state;
	}
	public List<Boolean> getInputs() {
		return inputs;
	}
	public void setInputs(List<Boolean> inputs) {
		this.inputs = inputs;
	}
	public List<Boolean> getRegisters() {
		return registers;
	}
	public void setRegisters(List<Boolean> registers) {
		this.registers = registers;
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	
	
	
	
}
