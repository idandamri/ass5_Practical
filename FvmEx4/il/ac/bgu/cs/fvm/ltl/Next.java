package il.ac.bgu.cs.fvm.ltl;

public class Next extends Ltl {
	private Ltl inner;

	public Next(Ltl inner) {
		this.setInner(inner);
	}

	public Ltl getInner() {
		return inner;
	}

	public void setInner(Ltl inner) {
		this.inner = inner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inner == null) ? 0 : inner.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Next))
			return false;
		Next other = (Next) obj;
		if (inner == null) {
			if (other.inner != null)
				return false;
		} else if (!inner.equals(other.inner))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "()" + inner;
	}
}
