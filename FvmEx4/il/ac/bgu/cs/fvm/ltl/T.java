package il.ac.bgu.cs.fvm.ltl;

public class T extends Ltl {
	@Override
	public int hashCode() {
		return 9876543;
	}

	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof And;
	}


	@Override
	public String toString() {
		return "true";
	}
}
