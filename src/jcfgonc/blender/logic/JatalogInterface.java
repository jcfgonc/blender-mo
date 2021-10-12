package jcfgonc.blender.logic;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import frames.SemanticFrame;
import graph.GraphReadWrite;
import graph.StringEdge;
import graph.StringGraph;
import za.co.wstoop.jatalog.DatalogException;
import za.co.wstoop.jatalog.Expr;
import za.co.wstoop.jatalog.Jatalog;

public class JatalogInterface {
	public static void main(String[] args) throws DatalogException {

		JatalogInterface ji = new JatalogInterface();

		String text = "partof(a,c)." + "ability(c,d).";
		StringGraph kb = new StringGraph();
		GraphReadWrite.readPrologFromString(text, kb);

		ji.addFacts(kb);

		text = "partof(A,C)." + "ability(C,D)." + "partof(B,C)." + "ability(C,E).";
		StringGraph frame = new StringGraph();
		GraphReadWrite.readPrologFromString(text, frame);

		List<Expr> q1 = JatalogInterface.createQueryFromStringGraph(frame);
		System.out.println(ji.isQueryTrue(q1));

		List<Expr> q2 = JatalogInterface.createQueryFromStringGraphUniqueInstantiation(frame);
		System.out.println(ji.isQueryTrue(q2));
	}

	public static List<List<Expr>> createQueriesFromFrames(ArrayList<SemanticFrame> frames) {
		// create frame queries
		int numFrames = frames.size();
		List<List<Expr>> frameQueries = new ArrayList<>(numFrames);
		for (int i = 0; i < numFrames; i++) {
			SemanticFrame frame = frames.get(i);
			StringGraph g = frame.getFrame();
			List<Expr> q = createQueryFromStringGraphUniqueInstantiation(g);
			frameQueries.add(q);
		}
		return frameQueries;
	}

	/**
	 * Obviously that each graph vertex must be a variable, otherwise Jatalog will not like it.
	 * 
	 * @param graph
	 * @return
	 */
	public static List<Expr> createQueryFromStringGraph(StringGraph graph) {
		// a query in jatalog is a list of expr (representing an conjunction of predicates)
		List<Expr> exprList = new ArrayList<Expr>();
		for (StringEdge edge : graph.edgeSet()) {
			String relation = edge.getLabel();
			String source = edge.getSource();
			String target = edge.getTarget();

			ArrayList<String> terms = new ArrayList<String>(2);
			terms.add(source);
			terms.add(target);

			Expr expr = new Expr(relation, terms);
			exprList.add(expr);
		}
		return exprList;
	}

	/**
	 * Obviously that each graph vertex must be a variable, otherwise Jatalog will not like it.
	 * 
	 * @param graph
	 * @return
	 */
	public static List<Expr> createQueryFromStringGraphUniqueInstantiation(StringGraph graph) {
		// a query in jatalog is a list of expr (representing an conjunction of predicates)
		List<Expr> exprList = createQueryFromStringGraph(graph);
		// add the disunification
		List<String> vars = new ArrayList<>(graph.getVertexSet());
		int nVars = vars.size();
		// now add the unique instantiation of variables
		for (int i = 0; i < nVars - 1; i++) {
			String var0 = vars.get(i);
			for (int j = i + 1; j < nVars; j++) {
				String var1 = vars.get(j);
				ArrayList<String> terms = new ArrayList<String>(2);
				terms.add(var0);
				terms.add(var1);
				// I wonder if it is not(X==Y)...
				Expr expr = new Expr("<>", terms);
				exprList.add(expr);
			}

		}
		return exprList;
	}

	private Jatalog jatalog;

	public JatalogInterface() {
		jatalog = new Jatalog();
	}

	public void addFact(String predicate, String source, String target) throws DatalogException {
		jatalog.fact(predicate, source, target);
	}

	public void addFact(StringEdge edge) throws DatalogException {
		String relation = edge.getLabel();
		String source = edge.getSource();
		String target = edge.getTarget();
		addFact(relation, source, target);
	}

	public void addFacts(Collection<StringEdge> facts) throws DatalogException {
		if (facts.isEmpty()) {
			System.err.println("addFacts(): no facts to add");
			return;
		}

		for (StringEdge edge : facts) {
			addFact(edge);
		}
	}

	public void addFacts(StringGraph facts) throws DatalogException {
		addFacts(facts.edgeSet());
	}

	/**
	 * Clears all the facts and rules existing in the database.
	 */
	public void clear() {
		jatalog.clear();
	}

	/**
	 * Clears all the facts existing in the database.
	 */
	public void clearFacts() {
		jatalog.clearFacts();
	}

	/**
	 * Clears all the rules existing in the database.
	 */
	public void clearRules() {
		jatalog.clearRules();
	}

	public int countQuerySolutions(List<Expr> query) throws DatalogException {
		Collection<Map<String, String>> bindings = query(query);
		return bindings.size();
	}

	public boolean isQueryTrue(List<Expr> query) throws DatalogException {
		Collection<Map<String, String>> bindings = query(query);
		boolean empty = bindings.isEmpty();
		return !empty;
	}

	public BitSet isQueryTrue(Collection<List<Expr>> queries) throws DatalogException {
		BitSet trueQueries = new BitSet(queries.size());
		int i = 0;
		for (List<Expr> query : queries) {
			if (isQueryTrue(query)) {
				trueQueries.set(i);
			}
			i++;
		}
		return trueQueries;
	}

	public Collection<Map<String, String>> query(List<Expr> query) throws DatalogException {
		Collection<Map<String, String>> bindings = jatalog.query(query);
		return bindings;
	}

}
