package il.ac.bgu.cs.fvm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.svvrl.goal.core.util.Colors;

import il.ac.bgu.cs.fvm.automata.Automaton;
import il.ac.bgu.cs.fvm.automata.MultiColorAutomaton;
import il.ac.bgu.cs.fvm.channelsystem.ChannelSystem;
import il.ac.bgu.cs.fvm.channelsystem.InterleavingActDef;
import il.ac.bgu.cs.fvm.channelsystem.ParserBasedInterleavingActDef;
import il.ac.bgu.cs.fvm.circuits.Circuit;
import il.ac.bgu.cs.fvm.exceptions.FVMException;
import il.ac.bgu.cs.fvm.labels.Action;
import il.ac.bgu.cs.fvm.labels.LabeledElement;
import il.ac.bgu.cs.fvm.labels.Location;
import il.ac.bgu.cs.fvm.labels.State;
import il.ac.bgu.cs.fvm.ltl.And;
import il.ac.bgu.cs.fvm.ltl.AtomicProposition;
import il.ac.bgu.cs.fvm.ltl.Ltl;
import il.ac.bgu.cs.fvm.ltl.Next;
import il.ac.bgu.cs.fvm.ltl.Not;
import il.ac.bgu.cs.fvm.ltl.T;
import il.ac.bgu.cs.fvm.ltl.Until;
import il.ac.bgu.cs.fvm.nanopromela.NanoPromelaFileReader;
import il.ac.bgu.cs.fvm.nanopromela.NanoPromelaLexer;
import il.ac.bgu.cs.fvm.nanopromela.NanoPromelaParser;
import il.ac.bgu.cs.fvm.nanopromela.NanoPromelaParser.OptionContext;
import il.ac.bgu.cs.fvm.nanopromela.NanoPromelaParser.StmtContext;
import il.ac.bgu.cs.fvm.programgraph.ActionDef;
import il.ac.bgu.cs.fvm.programgraph.ConditionDef;
import il.ac.bgu.cs.fvm.programgraph.PGTransition;
import il.ac.bgu.cs.fvm.programgraph.ProgramGraph;
import il.ac.bgu.cs.fvm.transitionsystem.Transition;
import il.ac.bgu.cs.fvm.transitionsystem.TransitionSystem;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Exercise4FacadeImplementation implements Exercise4Facade {
	@Override
	public ProgramGraph createProgramGraph() {
		return new ProgramGraphImp();
	}

	@Override
	public TransitionSystem createTransitionSystem() {
		return new TransitionSystemImp();
	}

	@Override
	public ProgramGraph interleave(ProgramGraph pg1, ProgramGraph pg2) {
		ProgramGraph ans = createProgramGraph();
		Iterator<Location> loc1 = pg1.getLocations().iterator();
		HashSet<Location[]> locationskartez= new HashSet<Location[]>();
		Set<Location> inpg1 = pg1.getInitialLocations();
		Set<Location> inpg2 = pg2.getInitialLocations();

		//add states and intial
		while(loc1.hasNext()){
			Location cur = loc1.next(); 
			Iterator<Location> loc2 = pg2.getLocations().iterator();
			while(loc2.hasNext()){
				Location cur2 = loc2.next();
				Location[] addLoc = new Location[2];
				addLoc[0]=cur;
				addLoc[1]=cur2;
				locationskartez.add(addLoc);
				ans.addLocation(new Location(cur.getLabel()+","+cur2.getLabel()));
				if(inpg1.contains(cur) && inpg2.contains(cur2)){
					ans.addInitialLocation(new Location(cur.getLabel()+","+cur2.getLabel()));//locationskartezInitial.add(addLoc);
				}
			}
		}

		//need to copy to ans
		Iterator<PGTransition> tr1 = pg1.getTransitions().iterator();
		Iterator<PGTransition> tr2 = pg2.getTransitions().iterator();
		HashSet<String> Uaction = new HashSet<String>();
		// action pg1 union pg2
		while(tr1.hasNext() || tr2.hasNext()){
			PGTransition cur;
			if(tr1.hasNext()){
				cur = tr1.next();
				Uaction.add(cur.getAction());
			}
			if(tr2.hasNext()){
				cur = tr2.next();
				Uaction.add(cur.getAction());
			}

		}

		//add initial states
		Set<List<String>> initilizationAns= new HashSet<List<String>>();
		Set<List<String>> initIter2 = pg2.getInitalizations();
		Iterator<List<String>> initIter1 = pg1.getInitalizations().iterator();
		Map<String,Set<String>> vars = new LinkedHashMap<String,Set<String>>();
		Iterator<List<String>> itrr2 = initIter2.iterator();
		//add to vars initilization pg1
		while(initIter1.hasNext()){
			List<String> cur = initIter1.next();
			for(int i=0;i<cur.size();i++){
				String[] var= getVarAsArray(cur.get(i));
				if(vars.containsKey(var[0]))
					vars.get(var[0]).add(var[1]);
				else{
					vars.put(var[0],new HashSet<String>());
					vars.get(var[0]).add(var[1]);
				}
			}
		}
		//add to vars initilization pg2
		while(itrr2.hasNext()){
			List<String> cur = itrr2.next();
			for(int i=0;i<cur.size();i++){
				String[] var= getVarAsArray(cur.get(i));
				if(vars.containsKey(var[0]))
					vars.get(var[0]).add(var[1]);
				else{
					vars.put(var[0],new HashSet<String>());
					vars.get(var[0]).add(var[1]);
				}
			}
		}
		buildInitial(initilizationAns,vars.entrySet().toArray(),0,new ArrayList<String>());
		Iterator<List<String>> initilizationAnsItr = initilizationAns.iterator();

		//add to ans initilization
		while(initilizationAnsItr.hasNext()){
			ans.addInitalization( initilizationAnsItr.next());
		}
		Iterator<Location[]> itr = locationskartez.iterator();
		while(itr.hasNext()){
			Location[] from = itr.next();
			Iterator<Location[]> itr2 = locationskartez.iterator();
			while(itr2.hasNext()){
				Location[] to = itr2.next();
				if(from[0].equals(to[0])){
					Set<PGTransition> ac=searchActions(from[1],to[1],pg2);
					Iterator<PGTransition> itrTrans = ac.iterator();
					while(itrTrans.hasNext()){
						PGTransition cur = itrTrans.next();
						Location locfrom = new Location(from[0].getLabel()+","+from[1].getLabel());
						Location locto = new Location(to[0].getLabel()+","+to[1].getLabel());
						ans.addTransition(new PGTransition(locfrom, cur.getCondition(), cur.getAction(), locto));
					}
				}
				if(from[1].equals(to[1])){
					Set<PGTransition> ac=searchActions(from[0],to[0],pg1);
					Iterator<PGTransition> itrTrans = ac.iterator();
					while(itrTrans.hasNext()){
						PGTransition cur = itrTrans.next();
						Location locfrom = new Location(from[0].getLabel()+","+from[1].getLabel());
						Location locto = new Location(to[0].getLabel()+","+to[1].getLabel());
						ans.addTransition(new PGTransition(locfrom, cur.getCondition(), cur.getAction(), locto));
					}
				}
			}

		}

		removeUnreachLoc(ans);



		return ans;
	}

	private void removeUnreachLoc(ProgramGraph ans){
		//check for reachable lcoations
		//		System.out.println(ans.getLocations().size());
		Set<Location> curloc = ans.getLocations();
		Set<Location> reachloc = reach(ans);


		//		System.out.println("initial post "+	post(ans,ans.getInitialLocations().iterator().next()));
		//		System.out.println("reachable::"+reachloc);
		//		
		//		 System.out.println("reach num:"+reachloc.size());
		HashSet<Location> needToDel = new HashSet<Location>(curloc);
		needToDel.removeAll(reachloc);

		//remove from transitions
		Iterator<PGTransition> transitr = ans.getTransitions().iterator();
		while(transitr.hasNext()){
			PGTransition cur = transitr.next();
			if(needToDel.contains(cur.getFrom()) || needToDel.contains(cur.getTo()))
				ans.removeTransition(cur);
		}
		//remove location
		Iterator<Location> needitr = needToDel.iterator();
		while(needitr.hasNext()){
			Location cur = needitr.next();
			ans.removeLocation(cur);

		}

	}

	@SuppressWarnings("unchecked")
	private void buildInitial(Set<List<String>> initilizationAns, Object[] vars,int i, ArrayList<String> arrayList) {
		if(i>=vars.length){
			initilizationAns.add(arrayList);
			return;
		}
		Entry<String, Set<String>> cur =(Entry<String, Set<String>>) vars[i];
		Iterator<String> itr = cur.getValue().iterator();
		while(itr.hasNext()){
			String val = itr.next();
			ArrayList<String> newarray = new ArrayList<String>();
			newarray.addAll(arrayList);
			String add = cur.getKey()+":="+val;
			newarray.add(add);
			buildInitial(initilizationAns,vars,i+1,newarray);
		}
	}

	private String[] getVarAsArray(String string) {
		String[] ans = new String[2];
		boolean flag=false;
		String a0 ="";
		String a1="";
		for(int i=0;i<string.length();i++){
			if(string.charAt(i)!=':' && flag==false){
				a0 =a0+ string.substring(i,i+1);
				continue;
			}
			if(flag && string.charAt(i)!='=')
				a1=a1+string.charAt(i);
			flag=true;
		}
		//test
		ans[0]=a0;
		ans[1]=a1;
		return ans;
	}

	private Set<Location> reach(ProgramGraph pg) {
		Set<Location> ans = new HashSet<Location>();
		reachrec(pg,ans,pg.getInitialLocations(), new HashSet<Location>());
		return ans;
	}

	private void reachrec(ProgramGraph pg,Set<Location> ans,Set<Location> initial,Set<Location> visited){
		if (!initial.isEmpty()){
			ans.addAll(initial);
			Iterator<Location> itr = initial.iterator();
			while(itr.hasNext()){
				Location cur = itr.next();
				if(!visited.contains(cur)){
					Set<Location> post = post (pg,cur);
					visited.add(cur);
					reachrec(pg,ans,post,visited);
				}
			}

		}
	}

	private Set<Location> post(ProgramGraph pg, Location loc) {
		HashSet<Location> ans = new HashSet<Location>();
		Iterator<PGTransition> transitr = pg.getTransitions().iterator();
		while(transitr.hasNext()){
			PGTransition cur = transitr.next();
			if(cur.getFrom().equals(loc))
				ans.add(cur.getTo());

		}
		return ans;
	}

	private Set<PGTransition> searchActions(Location location, Location location2, ProgramGraph pg2) {
		Iterator<PGTransition> trans = pg2.getTransitions().iterator();
		HashSet<PGTransition> ans = new HashSet<PGTransition>();

		while(trans.hasNext()){
			PGTransition cur = trans.next();
			if(cur.getFrom().equals(location) && cur.getTo().equals(location2))
				ans.add(cur);
		}

		return ans;
	}

	@Override
	public TransitionSystem interleave(TransitionSystem ts1, TransitionSystem ts2) {
		return interleave(ts1,ts2,new HashSet<Action>());
	}

	@Override
	public TransitionSystem interleave(TransitionSystem ts1, TransitionSystem ts2, Set<Action> hs) {

		if(ts1==null || ts2==null)
			return null;
		TransitionSystem ans = createTransitionSystem();
		HashSet<State[]> statescollect = new HashSet<State[]>();

		//		add actions
		Iterator<Action> ac1 = ts1.getActions().iterator();
		Iterator<Action> ac2 = ts2.getActions().iterator();
		while(ac1.hasNext() || ac2.hasNext()){

			if(ac1.hasNext())
				ans.addAction(ac1.next());

			if(ac2.hasNext())
				ans.addAction(ac2.next());
		}

		// ap U ap
		Iterator<String> ap1 = ts1.getAtomicPropositions().iterator();
		Iterator<String> ap2 = ts2.getAtomicPropositions().iterator();
		while(ap1.hasNext() || ap2.hasNext()){
			String ap = ap1.next();
			if  (ap!=null)
				ans.addAtomicProposition(ap);
			ap=ap2.next();
			if(ap!=null) 
				ans.addAtomicProposition(ap);
		}
		//add States
		Iterator<State> stitr1 = ts1.getStates().iterator();
		Map<State, Set<String>> lbl1 = ts1.getLabelingFunction();
		Map<State, Set<String>> lbl2 = ts2.getLabelingFunction();

		while(stitr1.hasNext()){
			State st1 = stitr1.next();
			Iterator<State> stitr2 = ts2.getStates().iterator();
			while(stitr2.hasNext()){
				State st2 = stitr2.next();
				State newstate = new State(st1.getLabel()+","+st2.getLabel());
				ans.addState(newstate);
				HashSet<String> ap = new HashSet<String>();
				Set<String> apst1 = lbl1.get(st1);
				if(apst1!=null)
					ap.addAll(apst1);

				Set<String> apst2 = lbl2.get(st2);
				if(apst2!=null)
					ap.addAll(apst2);


				Iterator<String> apit = ap.iterator();
				while(apit.hasNext()){
					ans.addLabel(newstate, apit.next());
				}

				//for trans 
				State[] add= new State[2];
				add[0]=st1;
				add[1]=st2;
				statescollect.add(add);
			}
		}

		//initial states

		Iterator<State> in1 = ts1.getInitialStates().iterator();
		while(in1.hasNext()){
			State s1 = in1.next();
			Iterator<State> in2 = ts2.getInitialStates().iterator();
			while(in2.hasNext()){
				State s2 = in2.next();
				State newstate = new State(s1.getLabel()+","+s2.getLabel());
				ans.addInitialState(newstate);
			}
		}


		//add transitions
		Action ac;
		Iterator<State[]> itst = statescollect.iterator();
		while(itst.hasNext()){
			State[] from = itst.next();
			Iterator<State[]> itst2 = statescollect.iterator();
			while(itst2.hasNext()){
				State[] to = itst2.next();
				ac=searchTrans(from[0], to[0], ts1);

				if (hs!=null && ac!=null && hs.contains(ac)){
					Action ac22 = searchTrans(from[1], to[1], ts2);
					if(ac22!=null && ac22.equals(ac)){
						ans.addTransition(new Transition(new State(from[0].getLabel()+","+from[1].getLabel()),ac,new State(to[0].getLabel()+","+to[1].getLabel())));
					}
				}
				else{
					if(from[1].equals(to[1])){
						ac=searchTrans(from[0], to[0], ts1);
						if (ac!=null && !hs.contains(ac)){
							ans.addTransition(new Transition(new State(from[0].getLabel()+","+from[1].getLabel()),ac,new State(to[0].getLabel()+","+to[1].getLabel())));
						}
					}
					if(from[0].equals(to[0])){
						ac=searchTrans(from[1], to[1], ts2);
						if (ac!=null  && !hs.contains(ac)){
							ans.addTransition(new Transition(new State(from[0].getLabel()+","+from[1].getLabel()),ac,new State(to[0].getLabel()+","+to[1].getLabel())));
						}
					}

				}
			}
		}
		removeUnreach(ans);
		return ans;
	}

	private void removeUnreach(TransitionSystem ts){
		Set<State> reachstates = reach(ts);
		Set<State> curstates = ts.getStates();
		HashSet<State> needtodelete = new HashSet<State>(curstates);
		needtodelete.removeAll(reachstates);
		Iterator<State> itr = needtodelete.iterator();
		while(itr.hasNext()){
			State cur = itr.next();
			Iterator<Transition> triter = searchTrans(ts, cur).iterator();
			//remove transitions use by this 
			while(triter.hasNext()){
				ts.removeTransition(triter.next());
			}
			//remove labled
			ts.getLabelingFunction().remove(cur);
			ts.removeState(cur);
		}

	}

	private Set<Transition> searchTrans(TransitionSystem ts, State s){
		Iterator<Transition> itr = ts.getTransitions().iterator();
		HashSet<Transition> ans = new HashSet<Transition>(); 
		while(itr.hasNext()){
			Transition cur = itr.next();
			State from = cur.getFrom();
			State to = cur.getTo();
			if(from.equals(s) || to.equals(s))
				ans.add(cur);
		}
		return ans;
	}

	private Action searchTrans(State st1, State st2, TransitionSystem ts1) {
		Iterator<Transition> iter = ts1.getTransitions().iterator();
		while(iter.hasNext()){
			Transition tr = iter.next();
			if(st1.getLabel().equals(tr.getFrom().getLabel()) && st2.getLabel().equals(tr.getTo().getLabel()))
				return tr.getAction();
		}
		return null;
	}

	@Override
	public boolean isActionDeterministic(TransitionSystem ts) {
		Iterator<Action> itsa;
		Iterator<State> states = ts.getStates().iterator();

		while(states.hasNext()){
			itsa= ts.getActions().iterator();

			while(itsa.hasNext()){
				if(post(ts,states.next(),itsa.next()).size()>1)
					return false;
			}
		}

		return true;


	}

	@Override
	public boolean isAPDeterministic(TransitionSystem ts) {
		Map<State, Set<String>> lbl = ts.getLabelingFunction();
		Iterator<State> itrs = ts.getStates().iterator();
		while(itrs.hasNext()){
			State state = itrs.next();
			Set<State> setPost = post(ts,state);
			Iterator<State> iterpost=setPost.iterator();
			while(iterpost.hasNext()){
				Set<String> ap = lbl.get(iterpost.next());
				boolean found=false;
				Iterator<State> itr2 = setPost.iterator();
				while(itr2.hasNext()){
					Set<String> ap2 = lbl.get(itr2.next());
					if(ap.containsAll(ap2) && ap2.containsAll(ap))
						found=true;
					if (found)
						return false;

				}
			}
		}
		return true;

	}

	@Override
	public boolean isExecution(TransitionSystem ts, List<LabeledElement> e) throws FVMException {
		return isInitialExecutionFragment(ts, e) && post(ts,new State(e.get(e.size()-1).getLabel())).isEmpty();
	}

	@Override
	public boolean isExecutionFragment(TransitionSystem ts, List<LabeledElement> e) throws FVMException {

		if (e.size()%2==0)
			return false;

		Set<State> states = ts.getStates();
		//check if the first is state
		if(!states.contains(new State(e.get(0).getLabel())))
			return false;

		if (e.size()==1)
			return true;

		//check in transitions
		Set<Transition> tr = ts.getTransitions();
		Iterator<LabeledElement> ite = e.iterator();

		//check state action state assume size>=3
		State st1 =new State( ite.next().getLabel());
		while(ite.hasNext()){
			Action ac= new Action( ite.next().getLabel());
			State st2 = new State( ite.next().getLabel());
			if(!tr.contains(new Transition(st1, ac, st2)))
				return false;
			st1=st2;

		}

		return true;
	}

	@Override
	public boolean isInitialExecutionFragment(TransitionSystem ts, List<LabeledElement> e) throws FVMException {
		State st = new State(e.get(0).getLabel());
		return  ts.getStates().contains(st) &&   ts.getInitialStates().contains(st)  && isExecutionFragment(ts,e);

	}

	@Override
	public boolean isMaximalExecutionFragment(TransitionSystem ts, List<LabeledElement> e) throws FVMException {
		return isExecutionFragment(ts, e) &&  post(ts,new State(e.get(e.size()-1).getLabel())).isEmpty();
	}

	@Override
	public boolean isStateTerminal(TransitionSystem ts, State s) throws FVMException {
		return post(ts,s).isEmpty();
	}

	@Override
	public Set<State> post(TransitionSystem ts, Set<State> c) throws FVMException {

		Iterator<State> it = c.iterator();
		Set<State> ans= new HashSet<State>();

		while(it.hasNext()){
			ans.addAll(post(ts,it.next()));
		}

		return ans;
	}

	@Override
	public Set<State> post(TransitionSystem ts, Set<State> c, Action a) throws FVMException {
		Iterator<State> it = c.iterator();
		Set<State> ans= new HashSet<State>();

		while(it.hasNext()){
			ans.addAll(post(ts,it.next(),a));
		}

		return ans;
	}

	@Override
	public Set<State> post(TransitionSystem ts, State s) {
		Iterator<Transition> it = ts.getTransitions().iterator();
		Set<State> ans= new HashSet<State>();

		while(it.hasNext()){
			Transition cur= it.next();
			if(cur.getFrom().equals(s))
				ans.add(cur.getTo());
		}
		return ans;
	}

	@Override
	public Set<State> post(TransitionSystem ts, State s, Action a) throws FVMException {
		Iterator<Transition> it = ts.getTransitions().iterator();
		Set<State> ans= new HashSet<State>();
		while(it.hasNext()){
			Transition cur= it.next();

			if(cur.getFrom().equals(s) && cur.getAction().equals(a))
				ans.add(cur.getTo());
		}

		return ans;

	}

	@Override
	public Set<State> pre(TransitionSystem ts, Set<State> c) throws FVMException {
		Iterator<State> it = c.iterator();
		Set<State> ans= new HashSet<State>();
		while(it.hasNext()){
			ans.addAll(pre(ts,it.next()));
		}
		return ans;
	}

	@Override
	public Set<State> pre(TransitionSystem ts, Set<State> c, Action a) throws FVMException {
		Iterator<State> it = c.iterator();
		Set<State> ans= new HashSet<State>();
		while(it.hasNext()){
			ans.addAll(pre(ts,it.next(),a));
		}
		return ans;
	}

	@Override
	public Set<State> pre(TransitionSystem ts, State s) throws FVMException {
		Iterator<Transition> it = ts.getTransitions().iterator();
		Set<State> ans= new HashSet<State>();
		while(it.hasNext()){

			Transition cur= it.next();

			if(cur.getTo().equals(s))
				ans.add(cur.getFrom());
		}
		return ans;
	}

	@Override
	public Set<State> pre(TransitionSystem ts, State s, Action a) throws FVMException {
		Iterator<Transition> it = ts.getTransitions().iterator();
		Set<State> ans= new HashSet<State>();
		while(it.hasNext()){
			Transition cur= it.next();
			if(cur.getTo().equals(s) && cur.getAction().equals(a))
				ans.add(cur.getFrom());
		}
		return ans;
	}

	@Override
	public Set<State> reach(TransitionSystem ts) {
		Set<State> ans = new HashSet<State>();
		reachrec(ts,ans,ts.getInitialStates());
		return ans;
	}

	private void reachrec(TransitionSystem ts,Set<State> ans,Set<State> initial){
		if (!initial.isEmpty()){
			Iterator<State> itr = initial.iterator();
			while(itr.hasNext()){
				State cur = itr.next();
				if (!ans.contains(cur)){
					Set<State> post = post (ts,cur);
					ans.add(cur);
					reachrec(ts,ans,post);
				}
			}
		}
	}

	@Override
	public TransitionSystem transitionSystemFromChannelSystem(ChannelSystem cs) {
		TransitionSystem ans = createTransitionSystem();
		List<ProgramGraph> pgs = cs.getProgramGraphs();
		InterleavingActDef ac= new ParserBasedInterleavingActDef();
		//need to check
		//Map<String, Object> eval = eval(init,new HashSet<ActionDef>(new ParserBasedInterleavingActDef());
		//		for(int i=0;i<initilaState.size();i++)
		//			buildTransAndStates(ans,pgs,eval,initilaState.get(i));

		Set<Location[]> allPossible=allpossibleStates(pgs); // initial
		Iterator<Location[]> itr = allPossible.iterator();
		while(itr.hasNext()){
			Location[] cur = itr.next();
			Iterator<Location[]> itr2 = allPossible.iterator();
			while(itr2.hasNext()){
				Location[] cur2 = itr2.next();
				Set<PGTransition> trans= new HashSet<PGTransition>();
				if(!addTransBetween(trans,cur,cur2,pgs))
					continue;

				Iterator<PGTransition> transitr = trans.iterator();
				String action="";
				while(transitr.hasNext()){
					PGTransition curtrans = transitr.next();
					action=action+curtrans.getAction();
					if(transitr.hasNext())
						action=action + " | ";

				}

				if(!ac.isOneSidedAction(action)){
					//need to add trans ans states
					//add states,ap,action,
					State newStateFrom=  newStateHelp(cur);// new State(cur.toString()); // need to be stata expected
					State newStateTo= newStateHelp(cur2);
					ans.addState(newStateFrom);
					ans.addState(newStateTo);
					ans.addAction(new Action(action));
					//System.out.println("action::"+action);
					Transition mewtrans= new Transition(newStateFrom, new Action(action), newStateTo);
					ans.addTransition(mewtrans);

				}
			}
		}
		return ans;
	}

	private State newStateHelp(Location[] loc) {
		String s="[location=";
		//[location=snd_msg(0),off,wait(0), eval={}]
		for(int i=0;i<loc.length;i++){
			s=s+loc[i].getLabel();
			if(i<loc.length-1)
				s=s+",";
		}
		s=s+"]";

		return new State(s);
	}

	private Set<Location[]> allpossibleStates(List<ProgramGraph> pgs) {
		Set<Location[]> ans = new HashSet<Location[]>();
		Location[] loc = new Location[pgs.size()];
		statesRec(ans,pgs,loc,0,pgs.size());
		return ans;
	}

	private void statesRec(Set<Location[]> ans,List<ProgramGraph> pgs, Location[] loc, int i, int size) {
		if(i>=size){
			ans.add(loc);
			return;	
		}
		ProgramGraph cur = pgs.get(i);
		Iterator<Location> itrStates = cur.getLocations().iterator();
		while(itrStates.hasNext()){
			Location curs = itrStates.next();
			Location[] newloc= new Location[size];
			copy(newloc,loc,i);
			newloc[i]=curs;
			statesRec(ans,pgs,newloc,i+1,size);
		}
	}

	private void copy(Location[] newloc, Location[] loc, int till) {
		for(int i=0;i<till;i++)
			newloc[i]=loc[i];
	}

	private boolean addTransBetween(Set<PGTransition> trans, Location[] from, Location[] to,List<ProgramGraph> pgs) {
		Set<PGTransition> newtrans = new HashSet<PGTransition>();

		for(int i=0;i<from.length;i++){
			Set<PGTransition> tr=searchTrans(pgs.get(i),from[i],to[i]);
			if(tr.isEmpty())
				return false;
			newtrans.addAll(tr);

		}
		trans.addAll(newtrans);
		return true;

	}

	private Set<PGTransition> searchTrans(ProgramGraph programGraph, Location from, Location to) {
		Iterator<PGTransition> itr = programGraph.getTransitions().iterator();
		HashSet<PGTransition> ans = new HashSet<PGTransition>();
		while(itr.hasNext()){
			PGTransition cur = itr.next();
			if(cur.getFrom().equals(from) && cur.getTo().equals(to))
				ans.add(cur);
		}
		return ans;
	}

	@Override
	public TransitionSystem transitionSystemFromCircuit(Circuit c) {
		TransitionSystem ans = createTransitionSystem();
		Set<List<List<Boolean>>> initialS = new HashSet<List<List<Boolean>>>();
		Set<List<List<Boolean>>> states= new HashSet<List<List<Boolean>>>();

		Action t = new Action("[true]");
		Action f = new Action("[false]");

		//action
		ans.addAction(t);
		ans.addAction(f);
		//atomic
		for(int i=0;i<c.getNumberOfInputPorts();i++){
			ans.addAtomicProposition("x"+(i+1));
		}
		for(int i=0;i<c.getNumberOfOutputPorts();i++){
			ans.addAtomicProposition("y"+(i+1));
		}
		for(int i=0;i<c.getNumberOfRegiters();i++){
			ans.addAtomicProposition("r"+(i+1));
		}
		//add sTates
		Set<List<Boolean>> inputs = subSetList(c.getNumberOfInputPorts());
		Set<List<Boolean>> regits = subSetList(c.getNumberOfRegiters());
		//createState kartez
		Iterator<List<Boolean>> itrs = inputs.iterator();
		while(itrs.hasNext()){
			List<Boolean> cur = itrs.next();
			Iterator<List<Boolean>> regs = regits.iterator();
			while(regs.hasNext()){
				List<Boolean> curreg = regs.next();
				List<List<Boolean>>	 add = new ArrayList<List<Boolean>>();
				add.add(cur);//first input
				add.add(curreg);//end is reg
				states.add(add);
				State toAdd=newState(cur,curreg);
				ans.addState(toAdd);
				//atomic
				List<Boolean> apOut = c.computeOutputs(curreg, cur);
				addAp(ans,toAdd,cur,curreg,apOut);
			}
		}
		//initial states
		itrs = inputs.iterator();
		while(itrs.hasNext()){
			List<Boolean> cur = itrs.next();
			List<List<Boolean>>	 add = new ArrayList<List<Boolean>>();
			add.add(cur);
			ArrayList<Boolean> curreg = new ArrayList<Boolean>();
			for(int i=0;i<c.getNumberOfRegiters();i++)
				curreg.add(false);
			add.add(curreg);
			initialS.add(add);
			State toAdd=newState(cur,curreg);
			ans.addInitialState(toAdd);
		}
		//transitions
		Iterator<List<List<Boolean>>> itr = states.iterator();
		while(itr.hasNext()){
			List<List<Boolean>> cur = itr.next();
			State from = newState(cur.get(0), cur.get(1));
			Iterator<List<Boolean>> itrin = subSetList(c.getNumberOfInputPorts()).iterator();
			while(itrin.hasNext()){
				List<Boolean> in = itrin.next();//action
				Action action=getAction(in);
				List<Boolean> reg = c.updateRegisters(cur.get(1), cur.get(0));
				State to = newState(in, reg);
				ans.addTransition(new Transition(from, action, to));
			}
		}
		removeUnreach(ans);
		return ans;
	}

	private Action getAction(List<Boolean> in) {
		String s="";
		for(int i=0;i<in.size();i++)
			s=s+"["+in.get(i)+"]";
		return new Action(s);
	}

	private void addAp(TransitionSystem ans,State s, List<Boolean> input, List<Boolean> reg, List<Boolean> out) {
		for(int i=0;i<input.size();i++)
			if(input.get(i)){
				ans.addLabel(s,"x"+(i+1));
			}
		for(int i=0;i<out.size();i++)
			if(out.get(i)){
				ans.addLabel(s,"y"+(i+1));
			}
		for(int i=0;i<reg.size();i++)
			if(reg.get(i)){
				ans.addLabel(s,"r"+(i+1));
			}
	}

	private State newState(List<Boolean> input, List<Boolean> reg) {

		//		State tf = new State("[registers=[true], inputs=[false]]");

		String s="[registers=";
		for(int i=0;i<reg.size();i++)
			s=s+"["+reg.get(0)+"]";
		s=s+", inputs=";
		for(int i=0;i<input.size();i++)
			s=s+"["+input.get(0)+"]";		
		s=s+"]";

		return new State(s);
	}

	private Set<List<Boolean>> subSetList(int size) {
		Set<List<Boolean>> ans = new HashSet<List<Boolean>>();
		genereateSub(ans,new ArrayList<Boolean>(),size);
		return ans;
	}

	private void genereateSub(Set<List<Boolean>> ans,List<Boolean> start, int size) {
		if(start.size()<size){
			List<Boolean> newPositon = new ArrayList<Boolean>(start);
			start.add(false);
			newPositon.add(true);
			genereateSub(ans,start,size);
			genereateSub(ans,newPositon,size);

		}
		else
			ans.add(start);

	}

	@SuppressWarnings("unchecked")
	@Override
	public TransitionSystem transitionSystemFromProgramGraph(ProgramGraph pg, Set<ActionDef> actionDefs,
			Set<ConditionDef> conditionDefs) {
		TransitionSystem ans = createTransitionSystem();
		Iterator<Location> itrinit = pg.getInitialLocations().iterator();
		List<Object[]> initialStates= new ArrayList<Object[]>();
		HashSet<State> visited = new HashSet<State>();
		//initial States
		while(itrinit.hasNext()){
			Location cur = itrinit.next();
			Iterator<List<String>> init = pg.getInitalizations().iterator();
			while(init.hasNext()){
				List<String> curinit = init.next();
				Map<String, Object> eval = eval(curinit,actionDefs);
				State newState=newState(cur,eval);
				Object[] add= new Object[2];
				add[0]=cur;
				add[1]=eval;
				initialStates.add(add);
				ans.addState(newState);
				//visited.add(cur);
				addApAndLabel(ans,newState,eval);
				ans.addInitialState(newState);
			}

		}
		// genereate states and trans from each initial state.
		for(int i=0;i<initialStates.size();i++)
			recState(ans,pg,(Location)initialStates.get(i)[0],(Map<String, Object>)initialStates.get(i)[1],
					actionDefs,
					conditionDefs,visited );
		return ans;
	}

	private void recState(TransitionSystem ans,ProgramGraph pg, Location location, Map<String, Object> eval,Set<ActionDef> actionDefs,
			Set<ConditionDef> conditionDefs,Set<State> visited) {
		Set<PGTransition> trans = serachTransByFrom(pg,location);
		if(trans==null)
			return;
		Iterator<PGTransition> transitr = trans.iterator();
		while(transitr.hasNext()){
			//System.out.println("visited:: "+visited.toString());
			PGTransition cur = transitr.next();
			String cond = cur.getCondition();
			String action = cur.getAction();
			Location to = cur.getTo();
			if(ConditionDef.evaluate(conditionDefs, eval, cond)){
				//add trans
				State newStateFrom=newState(location,eval);
				Map<String,Object> newEval = new HashMap<String,Object>(eval);
				if(ActionDef.isMatchingAction(actionDefs,action))
					newEval=ActionDef.effect(actionDefs, eval, action);
				State newStateTo=newState(to,newEval);
				ans.addState(newStateFrom);
				ans.addState(newStateTo);
				ans.addAction(new Action(action));
				Transition newTrans=new Transition(newStateFrom, new Action(action), newStateTo);
				addApAndLabel(ans,newStateTo,newEval);
				ans.addTransition(newTrans);
				//rec call
				visited.add(newStateFrom);
				if(!visited.contains(newStateTo))
					recState( ans, pg,  to,  newEval,actionDefs,
							conditionDefs,visited);
			}

		}

	}

	private void addApAndLabel(TransitionSystem ans, State newStateTo, Map<String, Object> eval) {
		Iterator<Entry<String, Object>> itr = eval.entrySet().iterator();
		while(itr.hasNext()){
			Entry<String, Object> cur = itr.next();
			String atomic=cur.getKey()+" = "+cur.getValue();
			ans.addAtomicProposition(atomic);
			ans.addLabel(newStateTo, atomic);
		}

	}

	private Set<PGTransition> serachTransByFrom(ProgramGraph pg, Location location) {
		Set<PGTransition> ans = new HashSet<PGTransition>();
		Iterator<PGTransition> itr = pg.getTransitions().iterator();
		while(itr.hasNext()){
			PGTransition cur = itr.next();
			if(cur.getFrom().equals(location))
				ans.add(cur);
		}
		return ans;
	}

	private Map<String,Object> eval(List<String> curinit, Set<ActionDef> actionDefs) {
		Map<String,Object> eval = new LinkedHashMap<String,Object>();
		for(int i=0;i<curinit.size();i++){
			Iterator<ActionDef> itr = actionDefs.iterator();
			while(itr.hasNext()){
				ActionDef cur = itr.next();
				if(cur.isMatchingAction(curinit.get(i))){
					eval=cur.effect(eval, curinit.get(i));
				}
			}
		}
		return eval;
	}

	private State newState(Location cur,Map<String, Object> eval) {
		String s="[location="+cur.getLabel()+", eval="+eval.toString()+"]";
		return new State(s);
	}

	@Override
	public ProgramGraph programGraphFromNanoPromela(String filename) throws Exception {
		StmtContext root = NanoPromelaFileReader.pareseNanoPromelaFile(filename);
		return mainNanoPromela(root);
	}

	@Override
	public ProgramGraph programGraphFromNanoPromelaString(String nanopromela) throws Exception {
		StmtContext root = NanoPromelaFileReader.pareseNanoPromelaString(nanopromela);
		return mainNanoPromela(root);
	}

	private ProgramGraph mainNanoPromela(StmtContext root){

		ProgramGraph ans = createProgramGraph();
		HashMap<Location, Set<PGTransition>> trans = new HashMap<Location,Set<PGTransition>>();
		Map<Location, Set<Location>> allsubs = new HashMap<Location, Set<Location>>();
		Map<Location, StmtContext> sc= new HashMap<Location, StmtContext>();
		Set<Location> States = sub(root,trans,allsubs,sc);
		//implement ininit and initial loc
		addStates(ans,States);
		ans.addInitalization(new ArrayList<String>());
		ans.addInitialLocation(newLocation(root.getText()));
		addTrans(ans,trans);

		removeUnreachLoc(ans);
		return ans;


	}

	private void addTrans(ProgramGraph ans, HashMap<Location, Set<PGTransition>> trans) {
		Iterator<Entry<Location, Set<PGTransition>>> itr = trans.entrySet().iterator();
		Set<Location> locs = ans.getLocations();
		while(itr.hasNext()){
			Entry<Location, Set<PGTransition>> cur = itr.next();
			if(locs.contains(cur.getKey())){
				Iterator<PGTransition> itrtrans = cur.getValue().iterator();
				while(itrtrans.hasNext()){
					PGTransition curtrans = itrtrans.next();
					if(locs.contains(curtrans.getTo()))
						ans.addTransition(curtrans);

				}

			}

		}
	}

	private void addStates(ProgramGraph ans, Set<Location> states) {
		Iterator<Location> itr = states.iterator();
		while(itr.hasNext()){
			Location cur = itr.next();
			ans.addLocation(cur);
		}

	}

	private Set<Location> sub(StmtContext root, Map<Location,Set<PGTransition>> trans,
			Map<Location,Set<Location>> allsubs
			,Map<Location,StmtContext> sc
			){


		//ArrayList<Location> lst = new ArrayList<Location>();
		HashSet<Location> sub = new HashSet<Location>();
		Location exit = new Location("[]");
		if (root.assstmt() != null || root.chanreadstmt() != null ||
				root.chanwritestmt() != null || root.atomicstmt() != null ||
				root.skipstmt() != null ){
			/* The sub-statements are only [root] and [exit] */
			Location loc= new Location("["+root.getText()+"]");
			sub.add(loc);
			sub.add(exit);
			String condition;
			String action;
			condition="";
			action="id";

			if(root.assstmt() != null){
				condition="";
				action=root.assstmt().VARNAME().getText()+":="+root.assstmt().intexpr().getText();
			}else if(root.chanreadstmt() != null){
				action=root.chanreadstmt().CHANNAME()+"?"+root.chanreadstmt().VARNAME();
			}else if(root.chanwritestmt() != null){
				action=root.chanwritestmt().CHANNAME()+"!"+root.chanwritestmt().intexpr().getText();
			}else if(root.skipstmt() != null){
				action="skip";
			}else if(root.atomicstmt() != null){
				action=root.getText();   // need tto check if ok
			}else if(root.atomicstmt() != null){
				action=root.atomicstmt().getText();
			}
			PGTransition newPGT = new PGTransition(loc, condition, action, exit);

			if(trans.get(loc)==null){
				trans.put(loc, new HashSet<PGTransition>());
			}
			trans.get(loc).add(newPGT);
			allsubs.put(loc, sub);
			sc.put(loc, root);
			return sub;
		}
		else if (root.ifstmt() != null) {
			/* The sub-statements are [root], [exit], and the sub-statements of
			 all op.stmt() where op is a member of root.ifstmt().option() */
			//			List<Location> condif = new ArrayList<Location>();
			Location condlocation = new Location("["+root.getText()+"]");
			sub.add(condlocation);
			sub.add(exit);
			List<OptionContext> options = root.ifstmt().option();
			for(int i=0;i<options.size();i++){
				StmtContext cur = options.get(i).stmt();
				Set<Location> substmt = new HashSet<Location>(sub(cur,trans,allsubs,sc));
				sub.addAll(substmt);
				String cond = root.ifstmt().option(i).boolexpr().getText();
				addTransIf(trans,condlocation,cur,substmt,cond);
			}

			allsubs.put(condlocation,sub);
			sc.put(condlocation,root);
			return sub;

		}
		else if (root.dostmt() != null) {
			List<OptionContext> options = root.dostmt().option();
			String looplocation =root.getText(); 
			Location looploc = new Location("["+looplocation+"]");
			sub.add(exit);
			sub.add(looploc);
			String conditions="";
			List<Location[]> newlocs= new ArrayList<Location[]>();
			for(int i=0;i<options.size();i++){
				String g = root.dostmt().option(i).boolexpr().getText();
				if(!g.equals(""))
					conditions=conditions+"("+g+")||";//newCond(,conditions);//meed to implement
				StmtContext stmt = options.get(i).stmt();
				Set<Location> substmt= new HashSet<Location>(sub(stmt,trans,allsubs,sc));
				substmt.remove(exit);
				Iterator<Location> itr = substmt.iterator();
				//genreate new location and save them
				while(itr.hasNext()){
					Location curitrStmtSub = itr.next();
					Location newloc=joinLocations(curitrStmtSub,looploc);
					Location[] add= new Location[2];
					add[0]=curitrStmtSub;
					add[1]=newloc;
					newlocs.add(add);
					sub.add(newloc);
				}
			}
			int len = conditions.length();
			if (len>0){
				conditions=conditions.substring(0,len-2);
				conditions="!("+conditions+")";
			}
			//transition loop->exit //need to improve
			Location fullooploc = newLocation (looplocation);
			PGTransition newpgt = new PGTransition(fullooploc, conditions, "", new Location("[]"));
			if(trans.get(fullooploc)==null){
				trans.put(fullooploc,new HashSet<PGTransition>());
			}
			trans.get(fullooploc).add(newpgt);
			allsubs.put(fullooploc,sub);
			sc.put(fullooploc, root);
			//generets the subs of the newlocs
			for(int i=0;i<newlocs.size();i++){
				Location[] cur = newlocs.get(i);
				//need to add to sc and subs
				Set<Location> subforstmt12=subComplex(cur[0],looploc,allsubs);
				if(allsubs.get(cur[1])==null)
					allsubs.put(cur[1],new HashSet<Location>());
				allsubs.get(cur[1]).addAll(subforstmt12);
				createContextCon(sc.get(cur[0]), sc.get(looploc), sc);
				//add trans ;
				addtransComp(trans, sc.get(cur[0]), sc.get(looploc), allsubs, sc);
			}
			//all the stmt are valued by sub in allsubs and in trans
			addTransLoop(trans,looplocation,options,allsubs,sc);
			return sub;
			/* The sub-statements are [root], [exit], and locations [sub;root]
where sub is a sub-statement of some op in root.dostmt().option() */
		}
		else { // ;
			/* The sub-statements are the union of locations of the form
			 [sub;root.stmt(1)] where sub is a sub-statement of root.stmt(0)
			 and of all the sub-statements of root.stmt(1) */
			StmtContext stmt1 = root.stmt(0);
			StmtContext stmt2 = root.stmt(1);
			Set<Location> subStmt2 = new HashSet<Location>(sub(stmt2,trans,allsubs,sc));
			Set<Location> subStmt1 = new HashSet<Location>(sub(stmt1,trans,allsubs,sc));
			subStmt1.remove(exit);
			Iterator<Location> itr = subStmt1.iterator();
			//[stmt1:stmt2 : stmt1-< sub{stmt1}\exit] U sub(stmt2)
			while(itr.hasNext()){
				Location cur = itr.next();
				Location newloc = joinLocations(cur, newLocation(stmt2.getText()));
				//need to add to sc and subs
				Set<Location> subforstmt12=subComplex(cur,newLocation(stmt2.getText()),allsubs);
				if(allsubs.get(newloc)==null)
					allsubs.put(newloc,new HashSet<Location>());
				allsubs.get(newloc).addAll(subforstmt12);
				createContextCon(sc.get(cur), sc.get(newLocation(stmt2.getText())), sc);
				addtransComp2(trans,cur,newLocation(stmt2.getText()),sc,allsubs);
				sub.add(newloc);
			}
			sub.addAll(subStmt2);
			Location stmt1stmt2=joinLocations(newLocation(stmt1.getText()),newLocation(stmt2.getText()));
			allsubs.put(stmt1stmt2,sub);
			sc.put(stmt1stmt2,root);
			//for the new stmt1;stmt2 between them
			addtransComp(trans,stmt1,stmt2,allsubs,sc);
			return sub;
		}
	}

	private Set<Location> subComplex(Location stmt1, Location stmt2, Map<Location, Set<Location>> allsubs) {
		Set<Location> ans = new HashSet<Location>();
		Iterator<Location> subsitr = allsubs.get(stmt1).iterator();
		while(subsitr.hasNext()){
			Location substmt = subsitr.next();
			if(!substmt.equals(new Location("[]"))){
				String s = getLabelLoc(substmt)+";"+getLabelLoc(stmt2);
				ans.add(new Location("["+s+"]"));
			}
		}
		ans.addAll(allsubs.get(stmt2));
		return ans;
	}

	private void addtransComp2(Map<Location, Set<PGTransition>> trans, Location stmt1, Location stmt2,
			Map<Location, StmtContext> sc, Map<Location, Set<Location>> allsubs) {
		Location stmt1loc = stmt1;
		Set<Location> sub = allsubs.get(stmt1);
		if(sub==null){
			System.out.println("sub null for "+stmt1.getLabel());
			if(sc.get(stmt1)==null)
				System.out.println("yesss");
		}
		if(sub!=null){
			Iterator<Location> subitr = sub.iterator();
			while(subitr.hasNext()){
				Location substmt = subitr.next();
				Iterator<PGTransition> itrtrans = trans.get(stmt1loc).iterator();
				while(itrtrans.hasNext()){
					PGTransition curtrans = itrtrans.next();
					if(curtrans.getTo().equals(substmt)){
						//need to add trans
						String froms = getLabelLoc(stmt1)+";"+getLabelLoc(stmt2);
						Location fromloc = newLocation(froms);
						Location toloc = joinLocations(substmt,stmt2);
						PGTransition newpgt = new PGTransition(fromloc, curtrans.getCondition(), curtrans.getAction(), toloc);
						if(trans.get(fromloc)==null)
							trans.put(fromloc, new HashSet<PGTransition>());
						trans.get(fromloc).add(newpgt);
						createContextCon(sc.get(stmt1), sc.get(stmt2), sc);
						//need to check
					}
				}
			}
		}
		else
			System.out.println("sub null in addtranscomp");
		//rule stmt1-->exit
		Iterator<PGTransition> itr = trans.get(stmt1).iterator();
		while(itr.hasNext()){
			PGTransition cur = itr.next();
			if(cur.getTo().equals(new Location("[]"))){
				String froms = getLabelLoc(stmt1)+";"+getLabelLoc(stmt2);
				Location fromloc = newLocation(froms);
				//				String to = stmt2.getText();
				Location toloc = stmt2;
				PGTransition newpgt = new PGTransition(fromloc, cur.getCondition(), cur.getAction(), toloc);
				if(trans.get(fromloc)==null)
					trans.put(fromloc, new HashSet<PGTransition>());
				trans.get(fromloc).add(newpgt);
				createContextCon(sc.get(stmt1), sc.get(stmt2), sc);
				//need to check
				//	sub(contex,trans,allsubs,sc);

			}
		}





	}

	private Location joinLocations(Location curitrStmtSub, Location looploc) {
		int len=curitrStmtSub.getLabel().length();
		String s1=curitrStmtSub.getLabel().substring(1, len-1);
		len = looploc.getLabel().length();
		String s2=looploc.getLabel().substring(1, len-1);
		return new Location("["+s1+";"+s2+"]");
	}

	private void addtransComp(Map<Location, Set<PGTransition>> trans
			, StmtContext stmt1, StmtContext stmt2,
			Map<Location, Set<Location>> allsubs, Map<Location, StmtContext> sc) {

		Location stmt1loc = newLocation(stmt1.getText());
		Set<Location> sub = allsubs.get(newLocation(stmt1.getText()));

		if(sub!=null){
			Iterator<Location> subitr = sub.iterator();
			while(subitr.hasNext()){
				Location substmt = subitr.next();
				Iterator<PGTransition> itrtrans = trans.get(stmt1loc).iterator();
				while(itrtrans.hasNext()){
					PGTransition curtrans = itrtrans.next();
					if(curtrans.getTo().equals(substmt)){
						//need to add trans
						String froms = stmt1.getText()+";"+stmt2.getText();
						Location fromloc = newLocation(froms);
						Location toloc = joinLocations(substmt,newLocation(stmt2.getText()));
						PGTransition newpgt = new PGTransition(fromloc, curtrans.getCondition(), curtrans.getAction(), toloc);
						if(trans.get(fromloc)==null)
							trans.put(fromloc, new HashSet<PGTransition>());
						trans.get(fromloc).add(newpgt);
						createContextCon(stmt1, stmt2, sc);
						//need to check
						//						sub(contex,trans,allsubs,sc);
					}
				}
			}
		}
		else
			System.out.println("sub null in addtranscomp");
		//rule stmt1-->exit
		Iterator<PGTransition> itr = trans.get(newLocation(stmt1.getText())).iterator();
		while(itr.hasNext()){
			PGTransition cur = itr.next();
			if(cur.getTo().equals(new Location("[]"))){
				String froms = stmt1.getText()+";"+stmt2.getText();
				Location fromloc = newLocation(froms);
				String to = stmt2.getText();
				Location toloc = newLocation(to);
				PGTransition newpgt = new PGTransition(fromloc, cur.getCondition(), cur.getAction(), toloc);
				if(trans.get(fromloc)==null)
					trans.put(fromloc, new HashSet<PGTransition>());
				trans.get(fromloc).add(newpgt);
				createContextCon(stmt1, stmt2, sc);
				//need to check
				//	sub(contex,trans,allsubs,sc);

			}
		}

	}

	private void addTransLoop(Map<Location, Set<PGTransition>> trans
			, String looplocation, List<OptionContext> options,
			Map<Location, Set<Location>> allsubs, 
			Map<Location, StmtContext> sc) {
		StmtContext root = sc.get(newLocation(looplocation));
		for(int i=0;i<options.size();i++){
			String g = root.dostmt().option(i).boolexpr().getText();
			StmtContext stmt1 = options.get(i).stmt();
			Set<Location> sub = allsubs.get(newLocation(stmt1.getText()));
			if(sub==null)
				System.out.println("error not found (allsubs) in addtransloop"+stmt1.getText());
			else{
				Iterator<Location> itrstmtsub = sub.iterator();
				while(itrstmtsub.hasNext()){
					Location curstmtsub = itrstmtsub.next();
					//find trans for stmt1->curstmtsub
					Set<PGTransition> t = trans.get(newLocation(stmt1.getText()));
					if(t!=null){
						Iterator<PGTransition> transitr = t.iterator();
						while(transitr.hasNext()){
							PGTransition curtrans = transitr.next();
							if(curtrans.getTo().equals(curstmtsub) && !curstmtsub.equals(new Location("[]"))){
								//add trans
								String cond = newCond(curtrans.getCondition(),g);
								Location looploc = newLocation(looplocation);
								String stmtSloop = getLabelLoc(curstmtsub)+";"+looplocation;
								Location stmtlooploc = newLocation(stmtSloop);
								PGTransition newpgt = new PGTransition(looploc, cond, curtrans.getAction(), stmtlooploc);

								if(trans.get(looploc)==null)
									trans.put(looploc, new HashSet<PGTransition>());
								trans.get(looploc).add(newpgt);
								StmtContext subcontext = sc.get(curstmtsub);
								StmtContext loopcontext = sc.get(newLocation(looplocation));
								if(subcontext==null || loopcontext==null)
									System.out.println("error context null in addtransloop");
								createContextCon(subcontext,loopcontext,sc);
								//add to the collections
								Set<Location> subforstmt12=subComplex(curstmtsub,looploc,allsubs);
								if(allsubs.get(stmtlooploc)==null)
									allsubs.put(stmtlooploc,new HashSet<Location>());
								allsubs.get(stmtlooploc).addAll(subforstmt12);
								createContextCon(sc.get(curstmtsub), sc.get(looploc), sc);
								addtransComp(trans,subcontext,loopcontext,allsubs,sc);
							}
							//stmt-->exit
							if(curtrans.getTo().equals(new Location("[]"))){
								String cond = newCond(curtrans.getCondition(),g);
								Location looploc = newLocation(looplocation);
								PGTransition newpgt = new PGTransition(looploc, cond, curtrans.getAction(), looploc);
								if(trans.get(looploc)==null)
									trans.put(looploc, new HashSet<PGTransition>());
								trans.get(looploc).add(newpgt);
							}
						}
					}
					else
						System.out.println("trans null in addtransloop");
				}
			}
		}
	}

	private String getLabelLoc(Location curstmtsub) {
		int len = curstmtsub.getLabel().length();
		return curstmtsub.getLabel().substring(1, len-1);
	}

	private StmtContext createContextCon(StmtContext stmt1, StmtContext stmt2,Map<Location, StmtContext> sc) {
		StmtContext stmt = new StmtContext(StmtContext.EMPTY, 131);
		stmt.addChild(stmt1);
		stmt.addChild(COL());
		stmt.addChild(stmt2);
		sc.put(newLocation(stmt.getText()), stmt);
		return stmt;
	}

	private TerminalNode COL() {
		StmtContext temp = NanoPromelaFileReader.pareseNanoPromelaString("x:=1; y:=1");
		return (TerminalNode) temp.getChild(1);
	}

	private Location newLocation(String s) {
		return new Location("["+s+"]");
	}

	private String newCond(String h, String alpa) {
		boolean bh=!h.equals("");
		boolean ba=!alpa.equals("");
		if(bh && !ba)
			return "("+h+")";
		if(ba && !bh)
			return "("+alpa+")";

		if(!ba && !bh)
			return "";
		return "("+alpa+") && ("+h+")";
	}

	private void addTransIf(Map<Location,Set<PGTransition>> trans,Location condlocation,StmtContext option,
			Set<Location> substmt,String cond){
		Location stmtloc =newLocation(option.getText());
		Set<PGTransition> pgts = trans.get(stmtloc);
		if(pgts==null)return;

		Iterator<PGTransition> itrpgts = pgts.iterator();

		while(itrpgts.hasNext()){
			PGTransition curpgt = itrpgts.next();
			Iterator<Location> itrsub = substmt.iterator();
			while(itrsub.hasNext()){
				Location cursubstmt = itrsub.next();
				Location tosubstmt = cursubstmt;//newLocation(cursubstmt);
				if(curpgt.getTo().equals(tosubstmt)){
					//need to add
					String newcond=newCond(curpgt.getCondition(), cond);
					PGTransition newpgt = new PGTransition(condlocation, newcond, curpgt.getAction(), tosubstmt);
					if(trans.get(condlocation)==null)
						trans.put(condlocation,new HashSet<PGTransition>());
					trans.get(condlocation).add(newpgt);
				}
			}
		}

	}

	@Override
	public ProgramGraph programGraphFromNanoPromela(InputStream inputStream) throws Exception {
		NanoPromelaLexer lexer = new NanoPromelaLexer(new ANTLRInputStream(inputStream));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		NanoPromelaParser parser = new NanoPromelaParser(tokens);
		ParserRuleContext tree = parser.stmt();
		StmtContext root = (StmtContext) tree.getRuleContext();
		return mainNanoPromela(root);
	}

	/********* ass5 ********/
	@Override
	public TransitionSystem product(TransitionSystem ts, Automaton aut) {
		Map<State, Map<Set<String>, Set<State>>> AUT_tranitions = aut.getTransitions();
		Set<State[]> initial_states= new HashSet<State[]>();
		Iterator<State> AUT_itr = aut.getInitialStates().iterator();
		Iterator<State> TS_itr = ts.getInitialStates().iterator();
		Map<State, Set<String>> lbl_Func = ts.getLabelingFunction();
		TransitionSystem toReturn = createTransitionSystem();

		while(TS_itr.hasNext()){
			State curr_trans = TS_itr.next();
			while(AUT_itr.hasNext()){
				State curr_aut = AUT_itr.next();
				Map<Set<String>, Set<State>> from_init_q = AUT_tranitions.get(curr_aut);
				if(from_init_q!=null)
				{
					Set<String> s = lbl_Func.get(curr_trans);
					if(s==null)
						s= new HashSet<String>();
					toReturn = handle_to_q(s,toReturn,from_init_q,curr_trans, initial_states);
				}
			}
		}
		// for each s in TS given, 
		Set<Transition> ts_transisions = ts.getTransitions();

		Iterator<State[]> iterator = initial_states.iterator();
		while(iterator.hasNext()) {
			State[] trans_element = iterator.next();
			generateTransProduct(toReturn,ts_transisions,trans_element,AUT_tranitions,lbl_Func ,new HashSet<State>());
		}
		return toReturn;
	}

	private TransitionSystem handle_to_q(Set<String> s, TransitionSystem toReturn, Map<Set<String>, Set<State>> from_init_q, State curr_trans,
			Set<State[]> initial_states) {
		Set<State> toq = from_init_q.get(s);
		if(toq!=null){
			Iterator<State> itrq = toq.iterator();
			while(itrq.hasNext()){
				State q = itrq.next();
				State[] add = {curr_trans,q};
				initial_states.add(add);
				State newState = new State(curr_trans.getLabel()+","+q.getLabel());
				toReturn.addAtomicProposition(q.getLabel());
				toReturn.addState(newState);
				toReturn.addLabel(newState, q.getLabel());
				toReturn.addInitialState(newState);
			}	
		}
		return toReturn;
	}

	private void generateTransProduct(TransitionSystem toReturn,Set<Transition> trans, State[] start,
			Map<State, Map<Set<String>, Set<State>>> autTrans, Map<State, Set<String>> lblFunc,Set<State> visited) {
		Iterator<Transition> iterator = trans.iterator();
		visited.add(new State(start[0].getLabel()+","+start[1].getLabel()));
		while(iterator.hasNext()){
			Transition curr_trans = iterator.next();
			if(curr_trans.getFrom().equals(start[0])){
				Set<String> lable = lblFunc.get(curr_trans.getTo());
				if(lable==null)
					lable=new HashSet<String>();
				Map<Set<String>, Set<State>> aut_trans = autTrans.get(start[1]);
				if(aut_trans==null)
					aut_trans=new HashMap<Set<String>,Set<State>>();
				Set<State> p_state = aut_trans.get(lable);//q->p
				if(p_state!=null){
					Iterator<State> iterp = p_state.iterator();
					while(iterp.hasNext()){
						State curr_aut_p = iterp.next();
						State next_state_t = curr_trans.getTo();
						//add states , add ap,add trans call with new state// add action
						State newState = new State(next_state_t.getLabel()+","+curr_aut_p.getLabel());
						toReturn.addState(newState);
						toReturn.addAtomicProposition(curr_aut_p.getLabel());
						toReturn.addAction(curr_trans.getAction());
						toReturn.addLabel(newState, curr_aut_p.getLabel());
						//add trans
						State from = new State(start[0].getLabel()+","+start[1].getLabel());
						Transition newTrans = new Transition(from, curr_trans.getAction(), newState);
						toReturn.addTransition(newTrans);
						//go with new state
						State[] new_start_state = {next_state_t, curr_aut_p};		
						if(!visited.contains(newState))
							generateTransProduct(toReturn, trans, new_start_state,autTrans,  lblFunc,visited);
					}
				}
			}
		}
	}

	@Override
	public Automaton GNBA2NBA(MultiColorAutomaton mulAut) {
		Set<State[]> initials= new HashSet<State[]>();
		Map<State, Map<Set<String>, Set<State>>> trans = mulAut.getTransitions();
		Automaton toReturn = new Automaton();
		Iterator<State> initialStatesItr = mulAut.getInitialStates().iterator();
		State curr_state = null;
		while(initialStatesItr.hasNext()){
			curr_state = initialStatesItr.next();
			State[] add = {curr_state, new State("1")};
			initials.add(add);
			toReturn.setInitial(new State(curr_state.getLabel()+",1"));
		}
		trans = mulAut.getTransitions();
		Map<Integer,Integer> map_states_colors = new HashMap<Integer,Integer>();
		Set<Integer> colors_range = mulAut.getColors();
		int range = colors_range.size();
		map_states_colors = map_colors(colors_range, map_states_colors);//maping the colors the colors
		//		Iterator<State> iterator = initials.iterator();
		//	    while(iterator.hasNext()) {
		//	    	State[] state_element = iterator.next();
		for (State[] state : initials)
			generateNbaRecursive(new HashSet<State>(),toReturn,range ,trans,map_states_colors,state,mulAut);
		Set<State> accepting = mulAut.getAcceptingStates(map_states_colors.get(1).intValue());
		Iterator<State> iterator = accepting.iterator();
		while(iterator.hasNext()) {
			State state_element = iterator.next();
			toReturn.setAccepting(new State(state_element.getLabel()+","+1));
		}
		return toReturn;
	}

	private Map<Integer, Integer> map_colors(Set<Integer> colors_range, Map<Integer, Integer> map_states_colors) {
		int i=1;
		Iterator<Integer> itrc = colors_range.iterator();
		while(itrc.hasNext()){
			map_states_colors.put(new Integer(i), itrc.next());
			i++;
		}
		return map_states_colors;		
	}

	public void generateNbaRecursive(Set<State> visited, Automaton toReturn ,int range, Map<State, Map<Set<String>, Set<State>>> transitions ,Map<Integer,Integer> states_colors,State[] start,MultiColorAutomaton mulAut){
		State state_start = new State(start[0].getLabel()+","+start[1].getLabel());
		visited.add(state_start);
		Map<Set<String>, Set<State>> trans_from = transitions.get(start[0]);
		if(trans_from==null)
			return ;
		Iterator<Entry<Set<String>, Set<State>>> iterator = trans_from.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Set<String>, Set<State>> alpha_curr_s = iterator.next();
			Set<String> alpha = alpha_curr_s.getKey();
			Set<State> sTag = alpha_curr_s.getValue();
			for(int j=1;j<=range;j++){
				int i = Integer.parseInt(start[1].getLabel());
				Integer colori = states_colors.get(new Integer(i));
				Set<State> accptedStates = mulAut.getAcceptingStates(colori);
				Set<State[]> newState=null;
				int curcolorJ = j;// mapping color
				if((!accptedStates.contains(start[0])) && i==curcolorJ){
					newState=addNewTransitions(start, toReturn,curcolorJ,alpha,sTag);// recursive
				}
				else if(accptedStates.contains(start[0]) && curcolorJ==((i%range)+1)){
					newState=addNewTransitions(start, toReturn,curcolorJ,alpha,sTag);// recursive
				}
				if(newState!=null){// recursive didn't return null
					Iterator<State[]> itrrec = newState.iterator();
					while(itrrec.hasNext()){
						State[] state_new = itrrec.next();
						State state_to = new State(state_new[0].getLabel()+","+state_new[1].getLabel());
						if(!visited.contains(state_to)){// recursive
							generateNbaRecursive(visited,toReturn,range, transitions, states_colors, state_new, mulAut);
						}
					}
				}
			}
		}
	}



	private Set<State[]> addNewTransitions( State[] start, Automaton toReturn, int curcolorJ, Set<String> alpha, Set<State> sTag) {
		Iterator<State> iterate_to = sTag.iterator();
		State state_from = new State(start[0].getLabel()+","+start[1].getLabel());
		Set<State[]> new_states_set_toReturn = new HashSet<State[]>();
		while(iterate_to.hasNext()){
			State curr_to = iterate_to.next();
			State state_new = new State(curr_to.getLabel()+","+curcolorJ);
			toReturn.addTransition(state_from, alpha, state_new);
			toReturn.addState(state_new);
			State[] toAr = {curr_to,new State(curcolorJ+"")};
			new_states_set_toReturn.add(toAr);
		}
		return new_states_set_toReturn;
	}


	@Override
	public Automaton LTL2BA(Ltl ltl) {
		MultiColorAutomaton toReturn = new MultiColorAutomaton();

		Set<Ltl> ltls_closure = new HashSet<Ltl>();
		Set<Ltl> AP = new HashSet<Ltl>();
		calcClosureHandler(ltl, ltls_closure,AP);
		Set<Set<Ltl>> startBgroup = new HashSet<Set<Ltl>>(); 
		Object[] ar = AP.toArray();// diffrent objects array - anoying problem!!!

		generateBaseGroupOfBsRec(0, ar,startBgroup,new HashSet<Ltl>());
		findAllB(ltl,startBgroup);// really needed?

		/*** 
		 * 
		debug prints: 

		System.out.println("BASE groups:"+startBgroup.size()+startBgroup);
		System.out.println("Batomic:"+AP);
		System.out.println("groups:"+startBgroup);
		System.out.println(startBgroup.size());
		System.out.println("closure:"+ltls_closure.size()+ltls_closure.toString());

		 ***/

		//Q0 and Q1
		Iterator<Set<Ltl>> start_group_iter = startBgroup.iterator();
		while(start_group_iter.hasNext()){
			Set<Ltl> set = start_group_iter.next();
			if(set.contains(ltl)){
				toReturn.setInitial(new State(set.toString()));
				System.out.println("initial state"+set);
			}
		}

		//final && trans
		//		System.out.println("final state:");
		int i=1;
		boolean color1=false;
		Iterator<Ltl> ltls_clo_iter = ltls_closure.iterator();
		while(ltls_clo_iter.hasNext()){
			Ltl curr_ltl = ltls_clo_iter.next();
			if (curr_ltl instanceof Until){
				for(Set<Ltl> set :startBgroup ){
					if(set.contains(((Until)curr_ltl).getRight())){
						color1=true;
						toReturn.setAccepting(new State(set.toString()), i);
						//						System.out.println(set);
					}
					else if(!set.contains(curr_ltl)){
						color1=true;
						toReturn.setAccepting(new State(set.toString()), i);
						//						System.out.println(set);
					} 
				}
				i++;// for color mapping 
			}
		}
		if(!color1){ // there is no until
			Iterator<Set<Ltl>> ltl_set_iter = startBgroup.iterator();
			while(ltl_set_iter.hasNext()){
				Set<Ltl> curr_ltl_set = ltl_set_iter.next();
				toReturn.setAccepting(new State(curr_ltl_set.toString()),0);
			}
		}

		for(Set<Ltl> B: startBgroup){//choose B index i from set of B's
			Set<Set<Ltl>> oper_intersection= new HashSet<Set<Ltl>>();
			Iterator<Ltl> ltls_clo_iter2 = ltls_closure.iterator();
			while(ltls_clo_iter2.hasNext()){
				Ltl l = ltls_clo_iter2.next();
				Set<Set<Ltl>> targetGroups= new HashSet<Set<Ltl>>();
				boolean flag=false;
				if(l instanceof Next){// O
					flag=true;
					nextRuleBIntersect(startBgroup, targetGroups, B, l);
				}else if(l instanceof Until){// U
					flag=true;
					untilRuleHandle(B,l,startBgroup,targetGroups);
				}
				if(flag){// if true there is new BtagGroup
					if(oper_intersection.size()!=0){
						oper_intersection.retainAll(targetGroups);	
					}
					else{
						oper_intersection.addAll(targetGroups);
					}
				}
			}
			//intersection contains all of the target groups
			Set<String> actions = new HashSet<String>();
			Set<Ltl> actions_from_ap = new HashSet<Ltl>(AP);
			actions_from_ap.retainAll(B);
			Iterator<Ltl> ltls_actions_iter = actions_from_ap.iterator();
			while(ltls_actions_iter.hasNext()){
				Ltl a = ltls_actions_iter.next();
				actions.add(a.toString());
			}
			/*** printing for debug ***/
			//			System.out.println("^^^^^^^^^^^^");
			for(Set<Ltl> cur : oper_intersection){
				toReturn.addTransition(new State(B.toString()), actions,new State(cur.toString()));
				//			System.out.println("from:"+B);
				//			System.out.println("to:"+cur);
			}
			//			System.out.println("^^^^^^^^^^^^");
		}
		return GNBA2NBA(toReturn);
	}


	private void nextRuleBIntersect(Set<Set<Ltl>> startBgroup, Set<Set<Ltl>> intersec, Set<Ltl> B, Ltl l) {
		Iterator<Set<Ltl>> ltl_iterator = startBgroup.iterator();
		while (ltl_iterator.hasNext()){
			Set<Ltl> B_tagging = ltl_iterator.next();
			if(!B.contains(l)){
				if (!B.contains(l)){
					if (!B_tagging.contains(((Next)l).getInner())){// O
						intersec.add(B_tagging);
					}
				}
			}
			else{
				if (B_tagging.contains(((Next)l).getInner())){// O
					intersec.add(B_tagging);
				}
			}
		}					
	}

	private void untilRuleHandle(Set<Ltl> B, Ltl l,Set<Set<Ltl>> startBgroup ,Set<Set<Ltl>> intersec) {
		Iterator<Set<Ltl>> ltl_B_iterator = startBgroup.iterator();
		if(B.contains(l)){
			if(!B.contains(((Until)l).getRight())){
				//check kilshun1 in b?
				if(B.contains(((Until)l).getLeft())){
					while (ltl_B_iterator.hasNext()){
						Set<Ltl> B_tagging = ltl_B_iterator.next();
						if (B_tagging.contains(((Until)l))){
							intersec.add(B_tagging);
						}
					}
				}
			}
			else{//take everything
				while (ltl_B_iterator.hasNext()){
					Set<Ltl> B_tagging = ltl_B_iterator.next();
					intersec.add(B_tagging);
				}
			}
		}
		else{
			if(!B.contains(l) && !B.contains(((Until)l).getRight())){
				while (ltl_B_iterator.hasNext()){
					Set<Ltl> B_tagging = ltl_B_iterator.next();
					if(B.contains(((Until)l).getLeft())){
						if (!B_tagging.contains(((Until)l))){
							intersec.add(B_tagging);
						}
					}else{
						intersec.add(B_tagging);
					}
				}
			}
		}
	}


	private void generateBaseGroupOfBsRec(int i, Object[] ar, Set<Set<Ltl>> startBgroup,Set<Ltl> curr_group) {
		if(i>=ar.length){
			startBgroup.add(curr_group);
			return;
		}
		else{
			Set<Ltl> new_not_group = new HashSet<Ltl>();
			new_not_group.addAll(curr_group);
			curr_group.add((Ltl)ar[i]);
			new_not_group.add(new Not((Ltl)ar[i]));
			generateBaseGroupOfBsRec(i+1,ar, startBgroup,curr_group);
			generateBaseGroupOfBsRec(i+1,ar, startBgroup,new_not_group);
		}
	}


	private void calcClosureHandler(Ltl ltl, Set<Ltl> closure_group,Set<Ltl> ap_group) {
		if (ltl instanceof AtomicProposition){
			closure_group.add(ltl);
			closure_group.add(new Not(ltl));
			ap_group.add(ltl);
		}
		else if (ltl instanceof Next){
			closure_group.add(ltl);
			closure_group.add(new Not(ltl));
			Next cast = (Next)ltl;
			calcClosureHandler(cast.getInner(), closure_group,ap_group);
		}
		else if (ltl instanceof Until){
			closure_group.add(ltl);
			closure_group.add(new Not(ltl));
			Until cast = (Until)ltl;
			calcClosureHandler(cast.getLeft(), closure_group,ap_group);
			calcClosureHandler(cast.getRight(), closure_group,ap_group);
		}	
		else if (ltl instanceof And){
			closure_group.add(ltl);
			closure_group.add(new Not(ltl));
			And cast = (And)ltl;
			calcClosureHandler(cast.getLeft(), closure_group,ap_group);
			calcClosureHandler(cast.getRight(), closure_group,ap_group);
		}
		else if (ltl instanceof Not){
			closure_group.add(ltl);
			Not cast = (Not)ltl;
			calcClosureHandler(cast.getInner() ,closure_group,ap_group);
		}
		else if (ltl instanceof T){
			closure_group.add(ltl);
		}
		else {
//			System.out.println("not found match");
			return;
		}
	}


	private void findAllB(Ltl ltl, Set<Set<Ltl>>startBgroup) {
		if (ltl instanceof AtomicProposition){
			return;// exit
		}	
		else if (ltl instanceof Next){
			Next cast = (Next)ltl;
			findAllB(cast.getInner(), startBgroup);
			Not ltlnot=new Not(ltl);
			Set<Set<Ltl>> temp = new HashSet<Set<Ltl>>();
			for(Set<Ltl> set : startBgroup){
				HashSet<Ltl> newNot = new HashSet<Ltl>(set);
				newNot.add(ltlnot);
				set.add(ltl);
				temp.add(newNot);
			}
			startBgroup.addAll(temp); 
		}
		else if (ltl instanceof And){
			And cast = (And)ltl;
			findAllB(cast.getLeft(),startBgroup);
			findAllB(cast.getRight(),startBgroup);
			Iterator<Set<Ltl>> itr = startBgroup.iterator();
			while(itr.hasNext()){
				Set<Ltl> cur = itr.next();
				if(cur.contains(cast.getLeft()) && cur.contains(cast.getRight()))
					cur.add(ltl);
				else
					cur.add(new Not(ltl));
			}
		}
		else if (ltl instanceof T){
			for(Set<Ltl> set : startBgroup){
				set.add(ltl);
			}
		}
		else if (ltl instanceof Not){
			Not cast = (Not)ltl;
			findAllB(cast.getInner(), startBgroup);
		}
		else if (ltl instanceof Until){
			Until cast = (Until)ltl;
			findAllB(cast.getLeft(),startBgroup);
			findAllB(cast.getRight(),startBgroup);
			Set<Set<Ltl>> temp = new HashSet<Set<Ltl>>();
			for(Set<Ltl> set : startBgroup){
				if(set.contains(cast.getRight()))
					set.add(ltl);
				else{
					if(!set.contains(cast.getLeft())){
						set.add(new Not(ltl));
					}
					else{ // splitting
						HashSet<Ltl> newGroup = new HashSet<Ltl>(set);
						newGroup.add(ltl);
						temp.add(newGroup);
						set.add(new Not(ltl));
					}
				}
			}
			startBgroup.addAll(temp);
		}
		else {
//			System.out.println("not found match");
			return;
		}
	}

}