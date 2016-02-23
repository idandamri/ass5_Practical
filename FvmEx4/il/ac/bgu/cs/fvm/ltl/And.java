package il.ac.bgu.cs.fvm.ltl;

public class And extends Ltl {
	private Ltl left;
	private Ltl right;

	
	public And(Ltl left, Ltl right) {
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


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof And))
			return false;
		And other = (And) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "(" + left + " /\\ " + right + ")";
	}

}
