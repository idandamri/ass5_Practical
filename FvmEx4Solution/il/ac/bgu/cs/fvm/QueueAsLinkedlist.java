package il.ac.bgu.cs.fvm;


import il.ac.bgu.cs.fvm.labels.State;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;


public class QueueAsLinkedlist implements Queue<State>{

	private LinkedList<State> q;
	
	public QueueAsLinkedlist() {
		q= new LinkedList<State>();
	}
	@Override
	public boolean addAll(Collection<? extends State> arg0) {
		return q.addAll(arg0);
	}

	@Override
	public void clear() {
		q.clear();
	}

	@Override
	public boolean contains(Object arg0) {
		return q.contains(q);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return q.isEmpty();
	}

	@Override
	public Iterator<State> iterator() {
		return q.iterator();
	}

	@Override
	public boolean remove(Object arg0) {
		return q.remove(arg0);
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		return q.size();
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(State arg0) {
		q.addLast(arg0);
		return true;
	}

	@Override
	public State element() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean offer(State arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public State peek() {

		return q.getFirst();
	}

	@Override
	public State poll() {
		return q.removeFirst();
	}

	@Override
	public State remove() {
		return null;
	}
	
	

}
