package il.ac.bgu.cs.fvm.ltl;

public class Until extends Ltl {
	private Ltl left;
	private Ltl right;

	
	public Until(Ltl left, Ltl right) {
		this.setLeft(left);
		this.setRight(right);
	}


	/**
	 * @return the left
	 */
	public Ltl getLeft() {
		return left;
	}


	/**
	 * @param left the left to set
	 */
	public void setLeft(Ltl left) {
		this.left = left;
	}


	/**
	 * @return the right
	 */
	public Ltl getRight() {
		return right;
	}


	/**
	 * @param right the right to set
	 */
	public void setRight(Ltl right) {
		this.right = right;
	}
	
	@Override
	public String toString() {
		return "(" + left + " U " + right + ")";
	}

}
