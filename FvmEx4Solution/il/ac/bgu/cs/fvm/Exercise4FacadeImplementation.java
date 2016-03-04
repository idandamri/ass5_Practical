package il.ac.bgu.cs.fvm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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
import il.ac.bgu.cs.fvm.nanopromela.NanoPromelaParser.StmtContext;
import il.ac.bgu.cs.fvm.programgraph.ActionDef;
import il.ac.bgu.cs.fvm.programgraph.ConditionDef;
import il.ac.bgu.cs.fvm.programgraph.PGTransition;
import il.ac.bgu.cs.fvm.programgraph.ProgramGraph;
import il.ac.bgu.cs.fvm.transitionsystem.Transition;
import il.ac.bgu.cs.fvm.transitionsystem.TransitionSystem;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;

public class Exercise4FacadeImplementation implements Exercise4Facade {

	@Override
	public ProgramGraph programGraphFromNanoPromela(String filename) throws Exception {
		ProgramGraph pg = createProgramGraph();
		NanoPromelaFileReader fileReader = new NanoPromelaFileReader();
		StmtContext context = fileReader.pareseNanoPromelaFile(filename);
		CreateStateAndTransition(pg,context);
		return pg;
	}

	private void CreateStateAndTransition(ProgramGraph pg, StmtContext context) {
		Location initialLocation = createLocation(context);
		pg.addLocation(initialLocation);
		pg.addLocation(new Location("[]"));
		pg.addInitialLocation(initialLocation);
		createAllStateAndTrans(pg,context,initialLocation);
	}

	private void createAllStateAndTrans(ProgramGraph pg, StmtContext context,Location location) {
		Map<Location,StmtContext> candidate = new LinkedHashMap<Location,StmtContext>();
		List<PGTransition> transitionList = createTransitionForContext(context,candidate, location);
		for (Map.Entry<Location, StmtContext> entry : candidate.entrySet())
		{
			if (!pg.getLocations().contains(entry.getKey()))
			{
				pg.addLocation(entry.getKey());
				createAllStateAndTrans(pg, entry.getValue(), entry.getKey());
			}
		}
		for (PGTransition t : transitionList)
		{
			pg.addTransition(t);
		}
	}

	private List<PGTransition> createTransitionForContext(StmtContext context, Map<Location,StmtContext> acc,Location location)
	{
		System.out.println(context.getText());
		List<PGTransition> ans = new ArrayList<PGTransition>();
		//if the root is atomic stmt
		if (context.atomicstmt()!=null)
		{

		}
		//if the root is if stmt
		if (context.ifstmt()!=null)
		{
			//we now build the transition for all the option
			PGTransition defaultTrans = new PGTransition();
			defaultTrans.setFrom(location);
			defaultTrans.setCondition("");
			ans.addAll(createTransitionForIfStemt(context,defaultTrans,acc));
		}

		if (context.dostmt()!=null)
		{
			ans.addAll(createTransitionForDostmt(context,acc,location));
		}

		if (context.assstmt()!=null)
		{
			ans.add(new PGTransition(createLocation(context),
					"",context.getText(),new Location("[]")));
		}

		//there is something wrong
		if (context.stmt().size()>0)
		{
			PGTransition trans = new PGTransition();
			trans.setFrom(location);
			trans.setCondition("");
			ans.addAll(createTransitionForConcatenation(trans,context,acc));
		}
		return ans;
	}

	private Collection<? extends PGTransition> createTransitionForDostmt(
			StmtContext context, Map<Location,StmtContext> acc, Location location) {
		System.out.println(context.getText());
		List<PGTransition> ans = new ArrayList<PGTransition>();
		for (int i=0; i<context.dostmt().option().size(); i++)
		{
			PGTransition trans = new PGTransition();
			trans.setFrom(location);
			trans.setCondition("");
			Collection<? extends PGTransition> tmpans = buildTransitionFromOptionDo(trans,context.dostmt().option(i).stmt(),context,acc);
			for (PGTransition t : tmpans)
			{
				if (t.getCondition().compareTo("")!=0)
					t.setCondition("("+context.dostmt().option(i).boolexpr().getText()+")" +" && " + "(" + t.getCondition() + ")");
				else
					t.setCondition("("+context.dostmt().option(i).boolexpr().getText()+")");
			}
			ans.addAll(tmpans);	
		}

		PGTransition trans = new PGTransition();
		trans.setFrom(createLocation(context));
		String condition = "";
		//creating exit statement
		if (context.dostmt().option().size()>0)
			condition = "("+context.dostmt().option(0).boolexpr().getText()+")";
		for (int i=1; i<context.dostmt().option().size(); i++)
		{
			condition = condition + "||"+"("+context.dostmt().option(i).boolexpr().getText()+")";
		}
		condition ="!("+condition+")";
		trans.setAction("");
		trans.setCondition(condition);
		trans.setTo(new Location ("[]"));
		ans.add(trans);
		return ans;
	}

	private Collection<? extends PGTransition> buildTransitionFromOptionDo(
			PGTransition trans, StmtContext option, StmtContext loop, Map<Location,StmtContext> acc ) {
		List <PGTransition> ans = new ArrayList<PGTransition>();
		System.out.println(option.getText());
		if (option.ifstmt()!=null)
		{
			for (int i=0; i<option.ifstmt().option().size(); i++)
			{
				PGTransition newtrans = new PGTransition();
				newtrans.setFrom(trans.getFrom());
				String cond = trans.getCondition();
				cond = "("+cond + " && "+"("+option.ifstmt().option(i).boolexpr().getText()+")";
				newtrans.setCondition(cond);
				ans.addAll(	buildTransitionFromOption(newtrans,option.ifstmt().option(i).stmt(),acc));
			} 
		}
		if (option.assstmt()!=null)
		{
			PGTransition newtrans = new PGTransition();
			newtrans.setFrom(trans.getFrom());
			String cond = trans.getCondition();
			newtrans.setCondition(cond);
			newtrans.setAction(option.getText());
			Location loc = createLocation(loop);
			newtrans.setTo(loc);
			acc.put(loc,loop);
			ans.add(newtrans);
		}
		if (option.stmt().size()>0)
		{

			PGTransition t = new PGTransition();
			t.setFrom(trans.getFrom());
			t.setCondition("");

			StmtContext innerElementInloop = createInnerConInLopp(option,loop);
			ans.addAll(createTransitionForConcatenation(t,innerElementInloop,acc));
		}
		if (option.atomicstmt()!=null)
		{
			PGTransition newtrans = new PGTransition();
			newtrans.setFrom(trans.getFrom());
			String cond = trans.getCondition();
			newtrans.setCondition(cond);
			newtrans.setAction(option.getText());
			Location loc = createLocation(loop);
			newtrans.setTo(loc);
			acc.put(loc,loop);
			ans.add(newtrans);
		}
		return ans;
	}

	private StmtContext createInnerConInLopp(StmtContext stmt,StmtContext loop) {
		StmtContext ans =new StmtContext(null, 131);
		for (int i=0 ; i<stmt.stmt().size(); i++)
		{
			ans.addChild(stmt.stmt(i));
			ans.stmt().add(stmt.stmt(i));
		}
		ans.addChild(loop);
		ans.stmt().add(loop);
		return ans;
	}

	//cond_cmd->option
	private Collection<? extends PGTransition> createTransitionForIfStemt(
			StmtContext context,PGTransition defaultTrans, Map<Location,StmtContext> acc) {
		List <PGTransition> ans = new ArrayList<PGTransition>();
		for (int i=0; i<context.ifstmt().option().size(); i++)
		{
			PGTransition trans = new PGTransition();
			//put the from location as the cond_cmd
			trans.setFrom(defaultTrans.getFrom());
			trans.setCondition("");
			//just for the brucket
			Collection<? extends PGTransition> tmpans = buildTransitionFromOption(trans,context.ifstmt().option(i).stmt(),acc);
			for (PGTransition t : tmpans)
			{
				if (t.getCondition().compareTo("")!=0)
					t.setCondition("("+context.ifstmt().option(i).boolexpr().getText()+")" +" && " + "(" + t.getCondition() + ")");
				else
					t.setCondition("("+context.ifstmt().option(i).boolexpr().getText()+")");
			}
			//now we need to find the action and the to location, and add new condition if needed
			ans.addAll(	tmpans);
		}
		return ans;
	}
	//now we need to find the action and the to location, and add new condition if needed
	//cond_cmd->condition1->null->null
	private Collection<? extends PGTransition> buildTransitionFromOption(PGTransition trans, StmtContext stmt, Map<Location,StmtContext> acc) {
		List <PGTransition> ans = new ArrayList<PGTransition>();
		System.out.println(stmt.getText());
		if (stmt.ifstmt()!=null)
		{
			ans.addAll(createTransitionForIfStemt(stmt,trans,acc));
		}
		if (stmt.assstmt()!=null || stmt.skipstmt()!=null)
		{
			trans.setAction(stmt.getText());			
			trans.setTo(new Location ("[]"));
			ans.add(trans);
		}
		if (stmt.dostmt()!=null)
		{
			ans.addAll(createIfDoTrans(trans,stmt,acc));
		}
		if (stmt.stmt().size()>0)
		{
			ans.addAll(createTransitionForConcatenation(trans,stmt,acc));
		}

		return ans;
	}

	//cond_cmd -> condition_i -> null -> null
	private Collection<? extends PGTransition> createIfDoTrans(
			PGTransition trans, StmtContext stmt, Map<Location, StmtContext> acc) {
		List <PGTransition> ans = new ArrayList<PGTransition>();
		System.out.println(stmt.getText());
		for (int i=0; i<stmt.dostmt().option().size(); i++)
		{
			PGTransition t = new PGTransition();
			t.setFrom(trans.getFrom());
			t.setCondition("");
			Collection<? extends PGTransition> tmpans = buildTransitionFromOptionDo(t,stmt.dostmt().option(i).stmt(),stmt,acc);
			for (PGTransition tmpT : tmpans)
			{
				if (tmpT.getCondition().compareTo("")!=0)
					tmpT.setCondition("("+stmt.dostmt().option(i).boolexpr().getText()+")" +" && " + "(" + tmpT.getCondition() + ")");
				else{
					String cond = stmt.dostmt().option(i).boolexpr().getText();
					tmpT.setCondition("("+stmt.dostmt().option(i).boolexpr().getText()+")");
				}
			}

			ans.addAll(tmpans);
		}

		PGTransition t = new PGTransition();
		t.setFrom(trans.getFrom());
		String condition = "";
		//creating exit statement
		for (int i=0; i<stmt.dostmt().option().size(); i++)
		{
			condition += "!(("+stmt.dostmt().option(i).boolexpr().getText()+"))";
		}
		t.setAction("");
		t.setCondition(condition);
		t.setTo(new Location ("[]"));
		ans.add(t);
		return ans;
	}

	private Collection<? extends PGTransition> createTransitionForConcatenation(
			PGTransition trans, StmtContext stmt, Map<Location,StmtContext> acc) {
		List <PGTransition> ans = new ArrayList<PGTransition>();
		//get the first stmt
		StmtContext first = stmt.stmt(0);
		//build the rest of the name that need to continue wht come back from first derive
		StringBuilder locName = new StringBuilder();
		for (int i =1; i<stmt.stmt().size(); i++)
		{
			System.out.println(stmt.stmt(i).getText());
			locName.append(";").append(stmt.stmt(i).getText());
		}
		//candidate will contain all the location the fisrt suppose to go to and we need to string the derive first
		Map <Location,StmtContext> candidate = new LinkedHashMap<Location,StmtContext>();
		Collection<? extends PGTransition> listTrans = createTransitionForContext(first,trans,candidate);
		for (PGTransition tmpTrans : listTrans)
		{
			PGTransition newTrans = new PGTransition();
			newTrans.setFrom(trans.getFrom());
			newTrans.setAction(tmpTrans.getAction());
			newTrans.setCondition(tmpTrans.getCondition());
			if (tmpTrans.getTo().equals(new Location("[]")))
			{	
				Location loc = null;
				if (locName.length()>0)
				{
					loc = new Location("["+locName.substring(1)+"]");
				}
				else
				{
					loc = new Location("[]");
				}

				StmtContext newStmt = createWithoutTheFirst(stmt);
				System.out.println(newStmt.getText());
				acc.put(loc,newStmt);
				newTrans.setTo(loc);				
			}
			else
			{
				Location loc = new Location(tmpTrans.getTo().getLabel().substring(0, tmpTrans.getTo().getLabel().length()-1) + locName.toString()+"]");
				Set<StmtContext> newStmt = createLIstWithNewirst(stmt, candidate,tmpTrans.getTo());
				for (StmtContext st : newStmt)
				{
					acc.put(loc,st);
				}
				newTrans.setTo(loc);
			}
			ans.add(newTrans);


		}
		return ans;
	}

	private StmtContext createWithoutTheFirst(StmtContext stmt) {
		System.out.println(stmt.getText());
		System.out.println(stmt.stmt().toString());
		StmtContext ans =new StmtContext(null, 131);
		for (int i=1 ; i<stmt.stmt().size(); i++)
		{
			ans.addChild(stmt.stmt(i));
			ans.stmt().add(stmt.stmt(i));
		}
		return ans;
	}

	private StmtContext createWithNewFirst(StmtContext stmt,StmtContext newstmt) {
		System.out.println(stmt.getText());
		System.out.println(stmt.stmt().toString());
		StmtContext ans =new StmtContext(null, 131);
		ans.addChild(newstmt);
		ans.stmt().add(newstmt);
		System.out.println(ans.getText());
		for (int i=1 ; i<stmt.stmt().size(); i++)
		{
			System.out.println(stmt.stmt(i).getText());
			ans.addChild(stmt.stmt(i));
			ans.stmt().add(stmt.stmt(i));
			System.out.println(ans.getText());

		}
		ans.getText();
		return ans;
	}

	private Set<StmtContext> createLIstWithNewirst(StmtContext stmt,Map<Location,StmtContext> newstmt, Location location) {
		Set<StmtContext> ans = new HashSet<StmtContext>();
		StmtContext context = newstmt.get(location);

		ans.add(createWithNewFirst(stmt, context));
		ans.forEach(action->{
			System.out.println(action.getText());
		});
		return ans;
	}

	private Location createLocation (StmtContext stmt)
	{
		System.out.println(stmt.getText());
		if (stmt.stmt().size()>0)
		{


			String name =  stmt.stmt(0).getText();

			for (int i =1; i<stmt.stmt().size(); i++)
			{
				name += ";"+stmt.stmt(i).getText();
			}
			return new Location ("["+name+"]");
		}
		return new Location ("["+stmt.getText()+"]");
	}

	private String createNameForStmtFirst(StmtContext stmt) {
		String name ="";
		System.out.println(stmt.getText());
		if (stmt.stmt().size()>1)
			name = createNameForStmtFirst(stmt.stmt(0)) + ";"+ createNameForStmtFirst(stmt.stmt(1)) + ";" +  name; 
		else
			return stmt.getText();
		return name;
	}

	private StmtContext createStmtContextFromString (String s)
	{
		PrintWriter writer;
		try {

			System.out.println(s);

			writer = new PrintWriter("tmp_file.np", "UTF-8");
			writer.println(s);
			writer.close();
			NanoPromelaFileReader fileReader = new NanoPromelaFileReader();
			StmtContext context = fileReader.pareseNanoPromelaFile("tmp_file.np");
			System.out.println(context.getText());
			return context;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	//we got here after if stmt meaning that from location in trans is the if
	private Collection<? extends PGTransition> createTransitionForContext(
			StmtContext first, PGTransition trans, Map<Location,StmtContext> acc) {
		List <PGTransition> ans = new ArrayList<PGTransition>();
		System.out.println(first.getText());
		if (first.ifstmt()!=null)
		{
			ans.addAll(createTransitionForIfStemt(first,trans,acc));
		}
		if (first.dostmt()!=null)
		{
			ans.addAll(createIfDoTrans(trans,first,acc));
		}
		if (first.assstmt()!=null)
		{
			PGTransition t = new PGTransition();
			t.setFrom(trans.getFrom());
			t.setCondition("");
			t.setAction(first.getText());
			t.setTo(new Location ("[]"));
			ans.add(t);
		}
		if (first.skipstmt()!=null)
		{
			PGTransition t = new PGTransition();
			t.setFrom(trans.getFrom());
			t.setCondition("");
			t.setAction(first.getText());
			t.setTo(new Location ("[]"));
			ans.add(t);
		}
		if (first.stmt().size()>0)
		{
			ans.addAll(createTransitionForConcatenation(trans,first,acc));
		}
		if (first.chanreadstmt()!=null)
		{
			PGTransition t = new PGTransition();
			t.setFrom(trans.getFrom());
			t.setCondition("");
			t.setAction(first.getText());
			t.setTo(new Location ("[]"));
			ans.add(t);
		}
		if (first.chanwritestmt()!=null)
		{
			PGTransition t = new PGTransition();
			t.setFrom(trans.getFrom());
			t.setCondition("");
			t.setAction(first.getText());
			t.setTo(new Location ("[]"));
			ans.add(t);
		}
		return ans;
	}

	private List<String> derive(StmtContext context)
	{
		List<String> subsetAcc = new ArrayList<String>();

		subsetAcc.add(context.getText());

		if (context.atomicstmt()!=null)
		{
			subsetAcc.addAll(atomicDervie(context));
		}
		if (context.ifstmt()!=null)
		{
			subsetAcc.addAll(ifDerive(context));			
		}
		if (context.dostmt()!=null)
		{
			subsetAcc.addAll(doDerive(context));			
		}
		if (context.stmt().size()>0)
		{
			for (int i = 0 ; i<context.stmt().size(); i++)
			{
				subsetAcc.addAll(derive(context.stmt(i)));
			}
		}



		return subsetAcc;


	}

	private String createRest(String[] splitContext) {
		StringBuilder ans = new StringBuilder();
		for (String s : splitContext)
		{
			ans.append(s);
		}
		return ans.toString();
	}


	private Collection<? extends String> atomicDervie(StmtContext context) {
		List<String> ans = new ArrayList<String>();

		return ans;
	}

	private Collection<? extends String> doDerive(StmtContext context) {
		List<String> ans = new ArrayList<String>();
		for (int i=0; i<context.dostmt().option().size(); i++)
		{
			ans.addAll(derive(context.dostmt().option(i).stmt()));
		}
		return ans;
	}

	private Collection<? extends String> ifDerive(StmtContext context) {
		List<String> ans = new ArrayList<String>();
		for (int i=0; i<context.ifstmt().option().size(); i++)
		{

			ans.addAll(derive(context.ifstmt().option(i).stmt()));
		}
		return ans;
	}

	@Override
	public ProgramGraph programGraphFromNanoPromelaString(String nanopromela) throws Exception {
		StmtContext context = createStmtContextFromString(nanopromela);
		ProgramGraph pg = createProgramGraph();
		CreateStateAndTransition(pg,context);
		return pg;
	}

	@Override
	public ProgramGraph createProgramGraph() {
		return new ProgramGraphImpl();
	}

	@Override
	public TransitionSystem createTransitionSystem() {
		return  new TransitionSystemImp();
	}

	@Override
	public ProgramGraph interleave(ProgramGraph pg1, ProgramGraph pg2) {
		ProgramGraph interleaveProgramGraph = createProgramGraph();
		createProgInterleaveLocationSet(interleaveProgramGraph,pg1,pg2);
		createProgramGraphIngterleaveInitialLocation(interleaveProgramGraph,pg1,pg2);
		createProgGraphInterleaveIntalization(interleaveProgramGraph,pg1,pg2);
		return interleaveProgramGraph;
	}


	private void createProgInterleaveLocationSet(
			ProgramGraph interleaveProgramGraph, ProgramGraph pg1,
			ProgramGraph pg2) {

		Set<List<Location>> initalPair = new HashSet<List<Location>>();
		pg1.getInitialLocations().forEach(init1 ->
		pg2.getInitialLocations().forEach(init2 -> {
			List<Location> pair = new ArrayList<Location>();
			pair.add(init1);
			pair.add(init2);
			initalPair.add(pair);
			interleaveProgramGraph.addLocation(createProgInterleaveLocation(init1,init2));
		}));

		helper(interleaveProgramGraph,pg1,pg2,initalPair);

	}

	private void helper (
			ProgramGraph interleaveProgramGraph, ProgramGraph pg1,
			ProgramGraph pg2, Set<List<Location>> currentLocations)
	{
		Set<PGTransition> trans = new HashSet<PGTransition>();
		Set<List<Location>> locationForNext = new HashSet<List<Location>>();
		List<Location> markLocation = null;
		Location curLocation = null;
		Location nextLocation = null;
		PGTransition tToAdd = null;
		Location goToLocation = null;
		if (currentLocations.size()>0)
		{
			for (List<Location> loc : currentLocations)
			{
				curLocation = createProgInterleaveLocation(loc.get(0), loc.get(1));
				trans = getTransByFromLoc(loc.get(0),pg1);
				for (PGTransition t : trans)
				{
					goToLocation = t.getTo();
					nextLocation = createProgInterleaveLocation(goToLocation, loc.get(1));
					if (!interleaveProgramGraph.getLocations().contains(nextLocation))
					{
						markLocation = new ArrayList<Location>();
						markLocation.add(goToLocation);
						markLocation.add(loc.get(1));
						locationForNext.add(markLocation);
						interleaveProgramGraph.addLocation(nextLocation);
					}
					tToAdd = new PGTransition(
							curLocation,t.getCondition(),t.getAction(),nextLocation);
					interleaveProgramGraph.addTransition(tToAdd);


				}
			}
			for (List<Location> loc : currentLocations)
			{
				curLocation = createProgInterleaveLocation(loc.get(0), loc.get(1));
				trans = getTransByFromLoc(loc.get(1),pg2);
				for (PGTransition t : trans)
				{
					goToLocation = t.getTo();
					nextLocation = createProgInterleaveLocation(loc.get(0),goToLocation);
					if (!interleaveProgramGraph.getLocations().contains(nextLocation))
					{
						markLocation = new ArrayList<Location>();
						markLocation.add(loc.get(0));
						markLocation.add(goToLocation);
						locationForNext.add(markLocation);
						interleaveProgramGraph.addLocation(nextLocation);
					}
					tToAdd = new PGTransition(
							curLocation,t.getCondition(),t.getAction(),nextLocation);
					interleaveProgramGraph.addTransition(tToAdd);

				}
			}
			helper(interleaveProgramGraph,pg1,pg2,locationForNext);
		}
	}

	private Set<PGTransition> getTransByFromLoc(Location location,
			ProgramGraph pg1) {
		Set<PGTransition> ans = new HashSet<PGTransition>();
		pg1.getTransitions().forEach(tran -> {
			if (tran.getFrom().equals(location))
			{
				ans.add(tran);
			}
		});
		return ans;
	}

	private List<String> oneInitilizeInterleave(List<String> init1, List<String> init2) {

		Map<String, Object> h1 = toMap(init1);
		toMap(init2).forEach((k,v)->h1.put(k, v));

		List<String> toReturn = new ArrayList<String>();
		h1.forEach((k,v)->toReturn.add(k+":="+v));
		// sort and reverse
		Collections.sort(toReturn);
		//Collections.reverse(toReturn);
		return toReturn;
	}

	private Map<String, Object> toMap(List<String> init1) {
		Map<String,Object> ans = new HashMap<String, Object>();
		init1.forEach(in ->
		{
			ans.put(in.substring(0, in.indexOf(":=")),
					in.substring(in.indexOf(":=")+2));
		});
		return ans;
	}

	private void createProgGraphInterleaveIntalization(
			ProgramGraph interleaveProgramGraph, ProgramGraph pg1,
			ProgramGraph pg2) {
		//		pg1.getInitalizations().forEach(init1->{
		//			pg2.getInitalizations().forEach(init2 ->{
		//				interleaveProgramGraph.addInitalization(oneInitilizeInterleave(init1,init2));
		//			});
		//		});
		Set<List<String>> ans = mergeToGather(pg1.getInitalizations(), pg2.getInitalizations());
		for (List<String> toAdd : ans)
		{
			Collections.sort(toAdd);
			Collections.reverse(toAdd);
			interleaveProgramGraph.addInitalization(toAdd);
		}
	}

	private void createProgramGraphIngterleaveInitialLocation(
			ProgramGraph interleaveProgramGraph, ProgramGraph pg1,
			ProgramGraph pg2) {
		pg1.getInitialLocations().forEach(initLocation1 ->
		pg2.getInitialLocations().forEach(initLocation2 ->
		interleaveProgramGraph.addInitialLocation(createProgInterleaveLocation(initLocation1,initLocation2))));
	}

	private Location createProgInterleaveLocation(Location initLocation1,
			Location initLocation2) {
		return new Location (initLocation1.getLabel()+","+initLocation2.getLabel());
	}

	@Override
	public TransitionSystem interleave(TransitionSystem ts1, TransitionSystem ts2) {
		TransitionSystem interLeaveTransitionSytem = createTransitionSystem();
		createInterleaveAtomicPropositionsSet(interLeaveTransitionSytem,ts1,ts2);
		createInterleveStateSet(interLeaveTransitionSytem,ts1,ts2);
		createInterleaveInitialStateSet(interLeaveTransitionSytem,ts1,ts2);
		createInterleaveActionsSet(interLeaveTransitionSytem,ts1,ts2);
		createInterleaveTransitionSet(interLeaveTransitionSytem,ts1,ts2);
		return interLeaveTransitionSytem;
	}

	private void createInterleaveTransitionSet(
			TransitionSystem interLeaveTransitionSytem, TransitionSystem ts1,
			TransitionSystem ts2) {

		ts1.getTransitions().forEach(trans -> ts2.getStates().forEach(s -> 
		interLeaveTransitionSytem.addTransition(
				new Transition(createInterleaveState(trans.getFrom(),s) ,
						trans.getAction(), 
						createInterleaveState(trans.getTo(),s)))));
		//	System.out.println("Ts2 Transitions: " + ts2.getTransitions());
		System.out.println(ts1.getStates());
		ts2.getTransitions().forEach(trans -> ts1.getStates().forEach(s -> 
		interLeaveTransitionSytem.addTransition(
				new Transition(createInterleaveState(s,trans.getFrom()) ,
						trans.getAction(), 
						createInterleaveState(s,trans.getTo())))));
	}

	private void createInterleaveActionsSet(
			TransitionSystem interLeaveTransitionSytem, TransitionSystem ts1,
			TransitionSystem ts2) {
		ts1.getActions().forEach(action -> interLeaveTransitionSytem.addAction(action));
		ts2.getActions().forEach(action -> interLeaveTransitionSytem.addAction(action));

	}

	private void createInterleaveInitialStateSet(
			TransitionSystem interLeaveTransitionSytem, TransitionSystem ts1,
			TransitionSystem ts2) {
		State newIntialState = null;
		for (State s1 : ts1.getInitialStates())
		{
			for (State s2 : ts2.getInitialStates())
			{
				//add the new state
				newIntialState = createInterleaveState (s1,s2);
				interLeaveTransitionSytem.addInitialState(newIntialState);
			}
		}

	}

	private void createInterleaveAtomicPropositionsSet(
			TransitionSystem interLeaveTransitionSytem, TransitionSystem ts1,
			TransitionSystem ts2) {
		ts1.getAtomicPropositions().forEach(ap -> interLeaveTransitionSytem.addAtomicProposition(ap));
		ts2.getAtomicPropositions().forEach(ap -> interLeaveTransitionSytem.addAtomicProposition(ap));


	}

	private void createInterleveStateSet(
			TransitionSystem interLeaveTransitionSytem, TransitionSystem ts1,
			TransitionSystem ts2) {
		//	State newState = null;
		Map<State,Set<String>> labelFunction1 = ts1.getLabelingFunction();
		Map<State,Set<String>> labelFunction2 = ts2.getLabelingFunction();
		Set<String> labels1 = null;
		Set<String> labels2 = null;
		for (State s1 : ts1.getStates())
		{
			for (State s2 : ts2.getStates())
			{
				State newState = createInterleaveState (s1,s2);
				interLeaveTransitionSytem.addState(newState);
				//add label function for this state
				labels1 = labelFunction1.get(s1);
				labels2 = labelFunction2.get(s2);
				if (labels1!=null)
					labels1.forEach(l -> interLeaveTransitionSytem.addLabel(newState, l));
				if (labels2!=null)
					labels2.forEach(l -> interLeaveTransitionSytem.addLabel(newState, l));
			}
		}		
	}

	private State createInterleaveState(State s1, State s2) {
		return new State (s1.getLabel()+","+s2.getLabel());
	}

	@Override
	public TransitionSystem interleave(TransitionSystem ts1, TransitionSystem ts2, Set<Action> hs) {
		TransitionSystem interLeaveTransitionSytem = createTransitionSystem();
		createInterleveStateSet(interLeaveTransitionSytem, ts1, ts2);
		createInterleaveInitialStateSet(interLeaveTransitionSytem, ts1, ts2);
		createInterleaveActionsSet(interLeaveTransitionSytem, ts1, ts2);
		createInterleaveInitialStateSet(interLeaveTransitionSytem, ts1, ts2);
		createInterleaveAtomicPropositionsSet(interLeaveTransitionSytem, ts1, ts2);
		createTransuctionWithH(interLeaveTransitionSytem,ts1,ts2,hs);
		removeUnReachable(interLeaveTransitionSytem);

		return interLeaveTransitionSytem;
	}

	private void removeUnReachable(TransitionSystem interLeaveTransitionSytem) {
		Set<State> reachableStates = reach(interLeaveTransitionSytem);
		for (State s : interLeaveTransitionSytem.getStates())
		{
			if (!reachableStates.contains(s))
			{
				removeTransuctionWithState(interLeaveTransitionSytem,s);
			}
		}
		interLeaveTransitionSytem.getStates().removeIf(state -> !reachableStates.contains(state));
	}

	private void removeTransuctionWithState(
			TransitionSystem ts, State s) {
		ts.getTransitions().removeIf(trans -> trans.getFrom().equals(s) || trans.getTo().equals(s));

	}

	private void createTransuctionWithH(
			TransitionSystem interLeaveTransitionSytem, TransitionSystem ts1,
			TransitionSystem ts2, Set<Action> hs) {
		addNotCommonTransuction(interLeaveTransitionSytem,ts1,ts2,hs);
		addNotCommonTransuction(interLeaveTransitionSytem,ts2,ts1,hs);
		addCommonTrunsuction(interLeaveTransitionSytem,ts1,ts2,hs);


	}

	private void addCommonTrunsuction(
			TransitionSystem interLeaveTransitionSytem, TransitionSystem ts1,
			TransitionSystem ts2, Set<Action> hs) {

		for (Action action : hs)
		{
			Set <Transition> transWithActionFromTs1;
			Set <Transition> transWithActionFromTs2;
			transWithActionFromTs1 = getFromTranasbyAction(action,ts1);
			transWithActionFromTs2 = getFromTranasbyAction(action,ts2);
			transWithActionFromTs1.forEach(trans1 ->
			transWithActionFromTs2.forEach(trans2 ->
			interLeaveTransitionSytem.addTransition(new Transition
					(createInterleaveState(trans1.getFrom(), trans2.getFrom()), 
							action, 
							createInterleaveState(trans1.getTo(), trans2.getTo())))));

		}
	}

	private void addNotCommonTransuction(
			TransitionSystem interLeaveTransitionSytem, TransitionSystem ts1,
			TransitionSystem ts2, Set<Action> hs) {
		for (Transition trans :  ts1.getTransitions())
		{
			//if the transuction not contain action that in hs we add it like before
			if (!hs.contains(trans.getAction()))
			{
				for (State s : ts2.getStates())
				{
					interLeaveTransitionSytem.addTransition(new Transition
							(createInterleaveState(trans.getFrom(), s), 
									trans.getAction(), 
									createInterleaveState(trans.getTo(), s)));
				}
			}
			//the action is common for both transition system
		}		
	}

	private Set<Transition> getFromTranasbyAction(Action action,TransitionSystem ts) {
		Set<Transition> ans = new HashSet<Transition>();
		ts.getTransitions().forEach(trans -> {
			if (trans.getAction().equals(action))
			{
				ans.add(trans);
			}
		});
		return ans;
	}

	@Override
	public boolean isActionDeterministic(TransitionSystem ts) {
		if (ts.getInitialStates().size()<=1)
		{
			for (Action a : ts.getActions())
			{
				for (State s : ts.getStates()){
					Set <State> tmpAns = post(ts,s,a);
					if (!(tmpAns==null || tmpAns.size()<=1))
						return false;
				}
			}
			return true;
		}
		else
			return false;	}

	@Override
	public boolean isAPDeterministic(TransitionSystem ts) {
		Set <State> postStates = null;
		List<State> postStatesArr  = null;
		Set <String> labels1;
		Set <String> labels2;
		//go over all state
		for (State s : ts.getStates())
		{
			//find there post
			postStates = post(ts,s);
			postStatesArr = toList(postStates);
			Map <State , Set <String> > labelFunction = ts.getLabelingFunction();
			//check if there is 2 post state with the same labelFunction 
			for (int i = 0; i<postStates.size(); i++)
				for (int j = i+1; j<postStates.size(); j++)
				{
					labels1 =labelFunction.get(postStatesArr.get(i));
					labels2 =labelFunction.get(postStatesArr.get(j));

					if (labels1!=null && labels2 != null && labels1.containsAll(labels2) && labels2.containsAll(labels1))
						return false;
				}
		}
		return true;
	}

	private List<State> toList(Set<State> postStates) {
		List <State> ans = new ArrayList<State>();
		for (State s : postStates)
		{
			ans.add(s);
		}
		return ans;
	}

	@Override
	public boolean isExecution(TransitionSystem ts, List<LabeledElement> e) throws FVMException {
		if ( isOddList(e) && 
				isValisStateAction (e) && 
				transitionExist(ts.getTransitions(),e) &&
				isInitialState(ts,e) &&
				isEndState(ts, e))
		{
			return true;		
		}
		return false;
	}

	@Override
	public boolean isExecutionFragment(TransitionSystem ts, List<LabeledElement> e) throws FVMException {
		if ( isOddList(e) && isValisStateAction (e) && transitionExist(ts.getTransitions(),e))
		{
			return true;		
		}
		return false;
	}

	@Override
	public boolean isInitialExecutionFragment(TransitionSystem ts, List<LabeledElement> e) throws FVMException {
		if ( isOddList(e) && 
				isValisStateAction (e) && 
				transitionExist(ts.getTransitions(),e) &&
				isInitialState(ts,e))
		{
			return true;		
		}
		return false;
	}

	private boolean isInitialState(TransitionSystem ts, List<LabeledElement> e) {
		if (e.size()>0)	
		{
			if (ts.getInitialStates().contains(e.get(0)))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isMaximalExecutionFragment(TransitionSystem ts, List<LabeledElement> e) throws FVMException {
		if ( isOddList(e) && 
				isValisStateAction (e) && 
				transitionExist(ts.getTransitions(),e) &&
				isEndState(ts, e))
		{
			return true;		
		}
		return false;
	}

	private boolean isEndState(TransitionSystem ts, List<LabeledElement> e) {
		State s = (State) e.get(e.size()-1);
		if (s!=null && post(ts,s).isEmpty())
			return true;

		return false;
	}

	private boolean transitionExist(Set<Transition> transitions,
			List<LabeledElement> e) {
		boolean found = false;
		if (e.size()>1)
		{
			for (int i=0; i<e.size()-2; i=i+2)
			{
				for (Transition t : transitions)
				{
					if (t.getFrom().equals(e.get(i)) &&
							t.getAction().equals(e.get(i+1)) &&
							t.getTo().equals(e.get(i+2)))
					{
						found = true;
						break;
					}
				}
				if (!found)
					return false;
			}
		}
		return true;
	}

	private boolean isValisStateAction(List<LabeledElement> e) {
		if (isState (e.get(0)))
		{
			for (int i=1; i<e.size(); i=i+2)
			{
				if (!(isAction (e.get(i)) && isState(e.get(i+1))))
					return false;
			}
			return true;
		}	
		return false;
	}

	private boolean isState(LabeledElement labeledElement) {
		return labeledElement instanceof State;
	}

	private boolean isAction(LabeledElement labeledElement) {
		return labeledElement instanceof Action;
	}

	private boolean isOddList(List<LabeledElement> e)
	{
		return e.size()%2 == 1;
	}

	@Override
	public boolean isStateTerminal(TransitionSystem ts, State s) throws FVMException {
		if (s!=null && post(ts,s).isEmpty())
			return true;

		return false;
	}

	@Override
	public Set<State> post(TransitionSystem ts, Set<State> c) throws FVMException {
		Set <Transition> transitions = ts.getTransitions();
		Set <State> ans = new HashSet<State> ();
		for (Transition t : transitions)
		{
			for (State s : c)
			{
				if (t.getFrom().equals(s))
					ans.add(t.getTo());
			}
		}
		return ans;
	}

	@Override
	public Set<State> post(TransitionSystem ts, Set<State> c, Action a) throws FVMException {
		Set <Transition> transitions = ts.getTransitions();
		Set <State> ans = new HashSet<State> ();
		for (Transition t : transitions)
		{
			for (State s : c)
			{
				if (t.getFrom().equals(s) && t.getAction().equals(a))
					ans.add(t.getTo());
			}
		}
		return ans;	
	}

	@Override
	public Set<State> post(TransitionSystem ts, State s) throws FVMException {
		Set <Transition> transitions = ts.getTransitions();
		Set <State> ans = new HashSet<State> ();
		for (Transition t : transitions)
		{

			if (t.getFrom().equals(s) )
				ans.add(t.getTo());

		}
		return ans;	
	}

	@Override
	public Set<State> post(TransitionSystem ts, State s, Action a) throws FVMException {
		Set <Transition> transitions = ts.getTransitions();
		Set <State> ans = new HashSet<State> ();
		for (Transition t : transitions)
		{

			if (t.getFrom().equals(s) && t.getAction().equals(a))
				ans.add(t.getTo());

		}
		return ans;	
	}

	@Override
	public Set<State> pre(TransitionSystem ts, Set<State> c) throws FVMException {
		Set <State> ans = new HashSet<State> ();

		for (State s : c)
		{
			ans.addAll(pre(ts,s));
		}
		return ans;
	}

	@Override
	public Set<State> pre(TransitionSystem ts, Set<State> c, Action a) throws FVMException {
		Set <State> ans = new HashSet<State> ();

		for (State s : c)
		{
			ans.addAll(pre(ts,s,a));
		}
		return ans;
	}

	@Override
	public Set<State> pre(TransitionSystem ts, State s) throws FVMException {
		Set <Transition> transitions = ts.getTransitions();
		Set <State> ans = new HashSet<State> ();
		for (Transition t : transitions)
		{

			if (t.getTo().equals(s) )
				ans.add(t.getFrom());

		}
		return ans;	
	}

	@Override
	public Set<State> pre(TransitionSystem ts, State s, Action a) throws FVMException {
		Set <Transition> transitions = ts.getTransitions();
		Set <State> ans = new HashSet<State> ();
		for (Transition t : transitions)
		{
			if (t.getTo().equals(s) && t.getAction().equals(a))
				ans.add(t.getFrom());
		}
		return ans;	
	}

	@Override
	public Set<State> reach(TransitionSystem ts) {
		Set<State> ans = new HashSet<State>();
		for (State s : ts.getInitialStates())
		{
			uniqeAdding(ans,getReachFromState(s,ts));
		}
		return ans;
	}

	private void uniqeAdding(Set<State> addTo,Set<State> toAdd) {
		for (State s : toAdd)
		{
			if (!addTo.contains(s))
			{
				addTo.add(s);
			}
		}
	}

	private Set<State> getReachFromState(State initialState, TransitionSystem ts) {

		Set <State> ans = new HashSet<State> ();
		Queue <State>q = new QueueAsLinkedlist();		
		q.add(initialState);
		ans.add(initialState);
		State checkState = null;

		Set<State> adj = null;
		while (!q.isEmpty())
		{
			checkState = q.poll();
			adj = post(ts, checkState);
			for (State currentAdjState : adj)
			{
				if (!ans.contains(currentAdjState))
				{
					q.add(currentAdjState);
					ans.add(currentAdjState);
				}
			}
		}
		return ans;
	}
	@Override
	
public TransitionSystem transitionSystemFromChannelSystem(ChannelSystem cs) {
		TransitionSystem ts = createTransitionSystem();
		List<ProgramGraph> pgs = cs.getProgramGraphs();
		InterleavingActDef iActDef = new ParserBasedInterleavingActDef();
		for (ProgramGraph pg : pgs)
			for (PGTransition t : pg.getTransitions()){
				if (iActDef.isMatchingAction(t.getAction()))
					System.out.println("match: " + t.getAction());
				if (iActDef.isOneSidedAction(t.getAction()))
					System.out.println("one side:"+t.getAction());
			}
		//create all the initial states
		createIntialStateForChannelSystem(cs,ts);
		createStatesChannelToTransition(cs,ts,ts.getStates());

		throw new UnsupportedOperationException();
	}

	private void createStatesChannelToTransition(ChannelSystem cs,
			TransitionSystem ts, Set<State> states) {
		// TODO Auto-generated method stub

	}
	private void createIntialStateForChannelSystem(ChannelSystem cs,
			TransitionSystem ts) {
		// TODO Auto-generated method stub

	}
	private void createIntialStateForChannelSystem(ChannelSystem cs) {
		Set<List<String>> initializtion = new HashSet<List<String>>();
		cs.getProgramGraphs().forEach(pg ->{
			//	initializtion.addAll(marge(initializtion,pg.getInitalizations())));
		});
	}

	private Set<List<String>> mergeToGather(Set<List<String>> initializtionAns, Set<List<String>> pgInitalizations) {
		Set<List<String>> firstMerge = marge(initializtionAns, pgInitalizations);
		Set<List<String>> secondMerge = marge(pgInitalizations, initializtionAns );
		Set<List<String>> ans = new HashSet<List<String>>();
		for (List<String> list1 : firstMerge)
		{
			if (secondMerge.contains(list1))
				ans.add(list1);
		}

		return ans;
	}

	private Set<List<String>> marge(
			Set<List<String>> initializtionAns, Set<List<String>> pgInitalizations) {
		Set<Map<String,Object>> oneInitAsMap = toSetOfMap(pgInitalizations);
		Set<Map<String,Object>> secondInitAsMap = toSetOfMap(initializtionAns);
		Set<Map<String,Object>> ans = new HashSet<Map<String,Object>> ();
		for (Map<String,Object> initFromOne : oneInitAsMap)
		{
			for (Map<String,Object> initFromSecond : secondInitAsMap)
			{
				boolean problem = false;
				Map<String,Object> initToAdd = copy(initFromOne);
				for (Map.Entry<String, Object> entry : initFromSecond.entrySet())
				{
					if (initFromOne.get(entry.getKey())==null)
						initToAdd.put(entry.getKey(), entry.getValue());
					else
					{
						String valOne = (String) initFromOne.get(entry.getKey()).toString();
						String valSecond = (String) entry.getValue().toString();
						if (valOne.compareTo(valSecond)==0)
							initToAdd.put(entry.getKey(), entry.getValue());
						else
						{
							problem=true;
							break;
						}
					}
				}
				if (!problem)
					ans.add(initToAdd);
			}
		}

		return toSetList(ans);
	}
	private Set<List<String>> toSetList(Set<Map<String, Object>> ans) {
		Set<List<String>> toReturn = new HashSet<List<String>>();
		ans.forEach(map ->{
			List<String> toAdd =  new ArrayList<String>();
			map.forEach((k,v)->{
				toAdd.add(k+"="+v);
			});
			toReturn.add(toAdd);
		});
		return toReturn;
	}
	private Map<String, Object> copy(Map<String, Object> initFromOne) {
		Map<String, Object> map = new HashMap<String,Object>();
		initFromOne.forEach((k,v)->{
			map.put(k,v);
		});
		return map;
	}
	private Set<Map<String, Object>> toSetOfMap(Set<List<String>> pgInitalizations) {
		Set<Map<String, Object>> ans = new HashSet<Map<String,Object>>();
		for (List<String> list : pgInitalizations)
		{
			Map<String, Object> hash = new HashMap<String, Object>();
			for (String val : list)
			{
				hash.put(val.substring(0,val.indexOf(":=")).replace(" ", ""),
						Integer.parseInt(val.substring(val.indexOf(":=")+2).replace(" ", "")));
			}
			ans.add(hash);
		}

		return ans;
	}
	private List<String> copyAndReplace(List<String> initList, String sub, String subAdded) {
		int index = initList.indexOf(subAdded);
		List<String> ans = new ArrayList<String>();

		for (int i=0; i<initList.size(); i++)
		{
			if (i==index)
				ans.add(sub);
			else
				ans.add(initList.get(i));
		}

		return ans;
	}
	@Override
	public TransitionSystem transitionSystemFromCircuit(Circuit c) {
		TransitionSystem circuitTransitionSystem = createTransitionSystem();
		buildCircuitAction(circuitTransitionSystem,c);
		createCirculeAtomic(circuitTransitionSystem,c);
		buildAllReacableStateAndTransition(circuitTransitionSystem,c);
		return circuitTransitionSystem;
	}
	private void createCirculeAtomic(TransitionSystem circuitTransitionSystem,
			Circuit c) {
		for (int i=1; i<=c.getNumberOfInputPorts(); i++)
		{
			circuitTransitionSystem.addAtomicProposition("x"+i);
		}
		for (int i=1; i<=c.getNumberOfOutputPorts(); i++)
		{
			circuitTransitionSystem.addAtomicProposition("y"+i);
		}
		for (int i=1; i<=c.getNumberOfRegiters(); i++)
		{
			circuitTransitionSystem.addAtomicProposition("r"+i);
		}
	}

	private void buildCircuitAction(TransitionSystem circuitTransitionSystem,
			Circuit c) {
		List<List<Boolean>> actionList = buildValues(c.getNumberOfInputPorts());
		actionList.forEach(action->{
			circuitTransitionSystem.addAction(new Action(action.toString()));
		});

	}

	private void buildAllReacableStateAndTransition(
			TransitionSystem circuitTransitionSystem, Circuit c) {
		List<CircuitStateData> statesDataInitial = new ArrayList<CircuitStateData>();
		List<CircuitStateData> statesData = new ArrayList<CircuitStateData>();

		Set <State> initalStates = buildIntialStates (c,statesDataInitial);
		initalStates.forEach(state -> {
			circuitTransitionSystem.addState(state);
			circuitTransitionSystem.addInitialState(state);
		});
		statesData.addAll(statesDataInitial);
		circuitBuildStateHelper (statesData,statesDataInitial, c,circuitTransitionSystem);
		computeFunctionLabel(circuitTransitionSystem,c,statesData);

	}

	private void computeFunctionLabel(TransitionSystem circuitTransitionSystem,
			Circuit c, List<CircuitStateData> statesData) {
		for (CircuitStateData stateData : statesData)
		{
			List<Boolean> outPuts = c.computeOutputs(stateData.getRegisters(), stateData.getInputs());

			List<String> labels = createCircuitLabel(outPuts,"y");
			labels.forEach(label -> {
				circuitTransitionSystem.addLabel(stateData.getState(), label);
			});
			labels = createCircuitLabel(stateData.getInputs(),"x");
			labels.forEach(label -> {
				circuitTransitionSystem.addLabel(stateData.getState(), label);
			});
			labels = createCircuitLabel(stateData.getRegisters(),"r");
			labels.forEach(label -> {
				circuitTransitionSystem.addLabel(stateData.getState(), label);
			});
		}
	}

	private List<String> createCircuitLabel(List<Boolean> values, String delimeter) {
		List<String> ans = new ArrayList<String>();
		for (int i = 1; i<=values.size(); i++)
			if (values.get(i-1))
				ans.add(delimeter+i);
		return ans;
	}

	private Set<State> buildIntialStates(Circuit c,List<CircuitStateData> statesData) {
		Set<State> ans = new HashSet<State>();
		List<List<Boolean>> inPutValue = buildValues(c.getNumberOfInputPorts());
		inPutValue.forEach(input -> {
			ans.add(createInterleaveState(input,c.getNumberOfRegiters(),statesData));
		});
		return ans;
	}

	private State createInterleaveState(List<Boolean> input,
			int numberOfRegiters,List<CircuitStateData> statesData) {
		StringBuilder str = new StringBuilder();

		List<Boolean> registerStream = new ArrayList<Boolean>();
		for (int i=0; i<numberOfRegiters; i++)
		{
			registerStream.add(false);
		}


		str.append("[registers=").append(registerStream.toString()).append(", inputs=").append(input.toString()).append("]");
		CircuitStateData stateData = new CircuitStateData(input, registerStream, new State (str.toString()));
		statesData.add(stateData);
		return stateData.getState();
	}

	private void circuitBuildStateHelper(List<CircuitStateData> statesDataCollector,List<CircuitStateData> statesData, Circuit c,TransitionSystem ts) {
		if (statesData.size()>0)
		{
			List<CircuitStateData> nextStatesData =  new ArrayList<CircuitStateData>();
			List<List<Boolean>> inputs = buildValues(c.getNumberOfInputPorts());
			for (CircuitStateData stateData : statesData)
			{
				for (List<Boolean> input : inputs){
					List<Boolean> updatedRegisters = c.updateRegisters(stateData.getRegisters(),stateData.getInputs());
					State state = createCircuitState(updatedRegisters,input);
					if (!ts.getStates().contains(state))
					{

						nextStatesData.add(new CircuitStateData(input, updatedRegisters, state));
						ts.addState(state);
					}
					Transition t = new Transition(stateData.getState(),new Action(input.toString()) , state);
					ts.addTransition(t);
				}
			}
			statesDataCollector.addAll(nextStatesData);

			circuitBuildStateHelper(statesDataCollector,nextStatesData, c, ts);
		}
	}

	private State createCircuitState(List<Boolean> updatedRegisters,
			List<Boolean> input) {
		StringBuilder str = new StringBuilder();
		str.append("[registers=").append(updatedRegisters.toString()).append(", inputs=").append(input.toString()).append("]");
		return new State(str.toString());
	}

	public List<List<Boolean>> buildValues(int numberOfValues)
	{
		List<List<Boolean>> ans = new ArrayList<List<Boolean>>();

		printBin("", numberOfValues,ans);
		return ans;
	}


	public void printBin(String soFar, int iterations,List<List<Boolean>> acc) {
		if(iterations == 0) {
			acc.add(createListBoolean(soFar));
		}
		else {
			printBin(soFar + "0", iterations - 1,acc);
			printBin(soFar + "1", iterations - 1,acc);
		}
	}

	private List<Boolean> createListBoolean(String string) {
		List<Boolean> ans = new ArrayList<Boolean>();
		for (int i=0; i<string.length(); i++)
		{
			ans.add(string.charAt(i)=='0'? false:true);
		}
		return ans;
	}


	@Override
	public TransitionSystem transitionSystemFromProgramGraph(ProgramGraph pg, Set<ActionDef> actionDefs,
			Set<ConditionDef> conditionDefs) {
		TransitionSystem ts = createTransitionSystem();
		State initialState = null;
		for (Location InitialLoc : pg.getInitialLocations())
		{
			for (List<String> intiializtion : pg.getInitalizations())
			{
				initialState = new State (buildProgToSysStateLabel(InitialLoc,asString(intiializtion)));
				ts.addState(initialState);
				ts.addInitialState(initialState);
			}
			if (pg.getInitalizations().isEmpty())
			{
				initialState = new State (buildProgToSysStateLabel(InitialLoc,""));
				ts.addState(initialState);
				ts.addInitialState(initialState);
			}
		}
		List<String> order = getOrder (pg.getInitalizations());
		//		Collections.reverse(order);
		createStatesFromPg(ts,pg,actionDefs,conditionDefs,ts.getInitialStates(),order);

		return ts;
	}

	private List<String> getOrder(Set<List<String>> initalizations) {
		List<String> init = null;
		List<String> ans = new ArrayList<String>();
		if (initalizations!=null)
		{
			if (initalizations.size()>0)
			{
				for (List<String> check : initalizations)
				{
					init = check;
					break;
				}
				for (String str : init)
				{
					if (str.indexOf(":=")>=0)
						ans.add(str.substring(0, str.indexOf(":=")));
					else
						if (str.indexOf("=")>=0)
							ans.add(str.substring(0, str.indexOf("=")));

				}
			}
		}
		return ans;
	}

	private void createStatesFromPg(TransitionSystem ts, ProgramGraph pg,
			Set<ActionDef> actionDefs, Set<ConditionDef> conditionDefs,
			Set<State> states,List<String> order) {

		Set<PGTransition> transitionForState = null;
		String action = null;
		String condition = null;
		Location goToLocation = null;
		Map<String,Object> paramValue = null;
		State nextState = null;
		Set<State> nextStates = new HashSet<State>();
		String evaluateAction = null;
		if (states.size()>0)
		{
			for (State currentState : states)
			{
				paramValue = stateToMap(currentState);
				addAtomicFromPg(currentState,paramValue,ts);
				transitionForState = getTransitionForState(currentState, pg);
				for (PGTransition trans : transitionForState)
				{
					action = trans.getAction();
					//					actionAsMap = toMap(action);
					condition = trans.getCondition();
					goToLocation = trans.getTo();

					if (ConditionDef.evaluate(conditionDefs, paramValue, condition))
					{
						//need to evaluagte the action
						evaluateAction = evaluate(action,paramValue,actionDefs,order);
						nextState = new State(buildProgToSysStateLabel2(goToLocation,evaluateAction));
						if (!ts.getStates().contains(nextState))
						{
							ts.addState(nextState);
							nextStates.add(nextState);
						}
						//need to add the action to the list
						ts.addAction(new Action (action));
						Transition newTrans = new Transition(currentState, new Action (action), nextState);
						ts.addTransition(newTrans);
					}
				}
			}
			createStatesFromPg(ts,pg,actionDefs,conditionDefs,nextStates,order);
		}
	}

	private void addAtomicFromPg(State currentState, Map<String, Object> paramValue,
			TransitionSystem ts) {		
		String atomic = null;
		for (Map.Entry<String, Object> entry : paramValue.entrySet())
		{
			atomic = entry.getKey()+ " = " +entry.getValue();
			ts.addAtomicProposition(atomic);
			ts.addLabel(currentState, atomic);
		}
	}

	private Map<String, Object> stateToMap(State currentState) {
		System.out.println(currentState.getLabel());
		String sub = currentState.getLabel();
		Map<String, Object> hash = new HashMap<String, Object>();
		int indexStart = sub.indexOf(",");
		sub = sub.substring(indexStart+1, sub.length()-1);
		indexStart = sub.indexOf("eval=");
		sub = sub.substring(indexStart+"eval=".length());
		sub = sub.substring(1, sub.length()-1);
		if (sub.compareTo("")!=0)
		{
			String[] subs = sub.split(",");

			for (String val : subs)
			{
				String value = val.substring(val.indexOf("=")+1).replace(" ", "");
				if (value.compareTo("null")==0)
				{
					hash.put(val.substring(0,val.indexOf("=")).replace(" ", ""),
							null) 
							;
				}
				else
				{
					int index = val.indexOf("=");
					if (index>=0){
						hash.put(val.substring(0,val.indexOf("=")).replace(" ", ""),
								Integer.parseInt(val.substring(val.indexOf("=")+1).replace(" ", "")))
								;
					}
					else
					{						
						hash.put(val, val);
					}
				}
			}
		}
		return hash;
	}

	private String evaluate(String action, Map<String, Object> actionAsMap, Set<ActionDef> actionDefs,List<String> order) {
		StringBuilder ans = new StringBuilder();
		ActionDef foundAction = null;
		Map<String, Object> actionResult = new HashMap<String, Object>();
		for (ActionDef actionDef : actionDefs)
		{
			if (actionDef.isMatchingAction(action))
			{
				foundAction = actionDef;
				break;
			}

		}
		actionResult = foundAction.effect(actionAsMap, action);
		List <String> asListAns = new ArrayList<String>();
		if (order.size()==0)
		{
			actionResult.forEach((k,v)->{
				order.add(k);
			});
		}
		for (String key : order)
		{
			asListAns.add(key+"="+actionResult.get(key));
		}

		ans.append(asListAns.toString());
		String a = ans.substring(1, ans.length()-1);
		return "{"+a+"}";
	}

	private Location getLocationFromState(State currentState, ProgramGraph pg) {
		String label = currentState.getLabel();
		int index = label.indexOf("location=")+"location=".length();
		label = label.substring(index);
		index = label.indexOf(", eval=");
		String ans = label.substring(0,index);
		for (Location loc : pg.getLocations())
		{
			if (loc.getLabel().compareTo(ans)==0)
			{
				return loc;
			}
		}
		return null;
	}

	private Set<PGTransition> getTransitionForState(State currentState, ProgramGraph pg)
	{
		Set<PGTransition> ans = new HashSet<PGTransition>();
		Location loc = getLocationFromState(currentState, pg);
		for (PGTransition trans : pg.getTransitions())
		{
			if (trans.getFrom().equals(loc))
			{
				ans.add(trans);
			}
		}
		return ans;
	}

	private String asString(List<String> toOneList) {
		StringBuilder ans = new StringBuilder();
		if (toOneList.size()>0)
		{
			ans.append(toOneList.get(0).replace(":", ""));
		}
		for (int i=1; i<toOneList.size(); i++)
		{
			ans.append(",").append(" ").append(toOneList.get(i).replace(":", ""));
		}
		return ans.toString();
	}

	private String buildProgToSysStateLabel(Location loc,
			String eval) {
		StringBuilder ans = new StringBuilder();
		ans.append("[location=").append(loc.getLabel()).append(", ").append("eval=")
		.append("{").append(eval).append("}").append("]");
		return ans.toString();
	}

	private String buildProgToSysStateLabel2(Location loc,
			String eval) {


		StringBuilder ans = new StringBuilder();
		ans.append("[location=").append(loc.getLabel()).append(", ").append("eval=")
		.append(eval).append("]");
		return ans.toString();
	}


	private void createInterleaveInitialziation(Set<List<String>> initializtion1,
			Set<List<String>> initializtion2)
	{

		initializtion1.forEach(init1 -> {
			initializtion2.forEach(init2 ->{
				Map<String,Object> initMap1 = toMap(init1);
				Map<String,Object> initMap2 = toMap(init2);
				Map<String,Set<Object>> commonArgs = new HashMap<String, Set<Object>>();
				Map<String,Object> notCommonArgs = new HashMap<String, Object>();

				initMap2.forEach((k,v)->{
					if (initMap1.get(k)==null)
						notCommonArgs.put(k, v);
					else
					{
						if (commonArgs.get(k)==null)
						{
							commonArgs.put(k, new HashSet<Object>());
						}
						commonArgs.get(k).add(v);
					}
				});

				initMap1.forEach((k,v)->{
					if (initMap2.get(k)==null)
						notCommonArgs.put(k, v);
					else
					{
						if (commonArgs.get(k)==null)
						{
							commonArgs.put(k, new HashSet<Object>());
						}
						commonArgs.get(k).add(v);
					}
				});

				makeAllCombination(commonArgs);
			});
		});
	}

	private void makeAllCombination(Map<String, Set<Object>> commonArgs) {
		List<List<String>> commonArgsAsString = new ArrayList<List<String>>();
		commonArgs.forEach((k,v) -> {
			List<String> argAsString = new ArrayList<String>();
			v.forEach(val ->{
				argAsString.add(k+"="+val);
			});
			commonArgsAsString.add(argAsString);
		});


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

	@Override
	public ProgramGraph programGraphFromNanoPromela(InputStream inputStream) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}