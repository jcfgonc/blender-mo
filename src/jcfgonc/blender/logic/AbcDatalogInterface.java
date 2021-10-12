package jcfgonc.blender.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import abcdatalog.ast.BinaryDisunifier;
import abcdatalog.ast.Clause;
import abcdatalog.ast.Constant;
import abcdatalog.ast.PositiveAtom;
import abcdatalog.ast.PredicateSym;
import abcdatalog.ast.Premise;
import abcdatalog.ast.Term;
import abcdatalog.ast.Variable;
import abcdatalog.ast.validation.DatalogValidationException;
import abcdatalog.engine.DatalogEngine;
import abcdatalog.engine.bottomup.sequential.SemiNaiveEngine;
import abcdatalog.util.substitution.ConstOnlySubstitution;
import graph.GraphReadWrite;
import graph.StringEdge;
import graph.StringGraph;

public class AbcDatalogInterface {

	public static void main(String[] args) throws DatalogValidationException {

		AbcDatalogInterface datalog = new AbcDatalogInterface();

		String text = "partof(a,c)." + "ability(c,d).";
		StringGraph kb = new StringGraph();
		GraphReadWrite.readPrologFromString(text, kb);

		datalog.addFacts(kb);

		text = "partof(A,C)." + "ability(C,D)." + "partof(B,C)." + "ability(C,E).";
		StringGraph frame0 = GraphReadWrite.readPrologFromString(text, new StringGraph());
		PositiveAtom q0 = datalog.addRuleFromStringGraphUniqueInstantiation("q0", frame0);
		
		text = "partof(A,C)." + "ability(C,D).";
		StringGraph frame1 = GraphReadWrite.readPrologFromString(text, new StringGraph());
		PositiveAtom q1 = datalog.addRuleFromStringGraphUniqueInstantiation("q1", frame1);
		datalog.initialize();

		System.out.println(datalog.query(q0));
		System.out.println(datalog.query(q1));

//		ArrayList<PositiveAtom> q1 = datalog.createQueryFromStringGraph(frame);
//		System.out.println(datalog.query(q1));

		// ArrayList<PositiveAtom> q2 = di.createRuleFromStringGraphUniqueInstantiation(String ruleId, StringGraph frame)
		// System.out.println(di.query(q2));
		System.lineSeparator();
	}

	private DatalogEngine abcDatalog;
	private static final List<Premise> emptyPremiseList = Collections.unmodifiableList(new ArrayList<>(0));
	private static final Term[] emptyTermArray = {};
	private Set<Clause> clauses;
	private boolean initialized;

	public AbcDatalogInterface() {
		abcDatalog = new SemiNaiveEngine();
		clauses = new HashSet<>(16, 0.333f);
		initialized = false;
	}

	public boolean isTermVariable(String s) {
		char c = s.charAt(0);
		if (Character.isUpperCase(c) || c == '_') {
			return true;
		}
		return false;
	}

	/**
	 * Forcefully creates a variable from the given string.
	 * 
	 * @param s
	 * @return
	 */
	private Variable createVariable(String s) {
		if (isTermVariable(s)) {
			return Variable.create(s);
		}
		throw new RuntimeException("Attempted to create a variable from " + s);
	}

	/**
	 * Forcefully creates a constant from the given string.
	 * 
	 * @param s
	 * @return
	 */
	private Constant createConstant(String s) {
		if (!isTermVariable(s)) {
			return Constant.create(s);
		}
		throw new RuntimeException("Attempted to create a constant from " + s);
	}

	/**
	 * Creates a constant or a Variable depending on the given string type.
	 * 
	 * @param s
	 * @return
	 */
	private Term createTerm(String s) {
		if (isTermVariable(s)) {
			return Variable.create(s);
		}
		return Constant.create(s);
	}

	public void addFacts(Collection<StringEdge> facts) throws DatalogValidationException {
		if (facts.isEmpty()) {
			System.err.println("addFacts(): no facts to add");
			return;
		}
		for (StringEdge edge : facts) {
			String source = edge.getSource();
			String relation = edge.getLabel();
			String target = edge.getTarget();

			// graph vertices are forcibly created as constants
			Constant[] constants = new Constant[2];
			constants[0] = createConstant(source);
			constants[1] = createConstant(target);
			PositiveAtom head = PositiveAtom.create(PredicateSym.create(relation, 2), constants);
			clauses.add(new Clause(head, emptyPremiseList));
		}
	}

	public void addFacts(StringGraph facts) throws DatalogValidationException {
		addFacts(facts.edgeSet());
	}

	/**
	 * initializes abdDatalog with the previously added facts/rules
	 * 
	 * @throws DatalogValidationException
	 */
	public void initialize() throws DatalogValidationException {
		abcDatalog.init(clauses);
		initialized = true;
	}

	/**
	 * Obviously that each graph vertex must be a variable.
	 * 
	 * @param graph
	 * @return
	 * @return
	 */
	public ArrayList<PositiveAtom> createQueryFromStringGraph(StringGraph graph) {
		ArrayList<PositiveAtom> query = new ArrayList<PositiveAtom>(graph.numberOfEdges() * 2);
		for (StringEdge edge : graph.edgeSet()) {
			String relation = edge.getLabel();
			String source = edge.getSource();
			String target = edge.getTarget();

			ArrayList<String> terms = new ArrayList<String>(2);
			terms.add(source);
			terms.add(target);

			Term[] constants = new Term[2];
			constants[0] = createTerm(source);
			constants[1] = createTerm(target);
			PositiveAtom term = PositiveAtom.create(PredicateSym.create(relation, 2), constants);
			query.add(term);
		}
		return query;
	}

	/**
	 * Obviously that each graph vertex must be a variable. Returns the ruleId rule term (head of the clause).
	 * 
	 * @param graph
	 * @return
	 * @return
	 */
	public PositiveAtom addRuleFromStringGraphUniqueInstantiation(String ruleId, StringGraph graph) {
		// ruleId := baseQuery, A!=B, A!=C, ...
		PositiveAtom head = PositiveAtom.create(PredicateSym.create(ruleId, 0), emptyTermArray);

		ArrayList<Premise> body = new ArrayList<Premise>();

		// create the base query a(X,Y...), b(...), ...
		body.addAll(createQueryFromStringGraph(graph));

		// add the disunification(s)
		ArrayList<String> vars = new ArrayList<>(graph.getVertexSet());
		int nVars = vars.size();
		// now add the unique instantiation of variables
		for (int i = 0; i < nVars - 1; i++) {
			String var0 = vars.get(i);
			Variable t0 = createVariable(var0);
			for (int j = i + 1; j < nVars; j++) {
				String var1 = vars.get(j);
				Variable t1 = createVariable(var1);
				BinaryDisunifier bd = new BinaryDisunifier(t0, t1);
				body.add(bd);
			}
		}
		// create the rule and add it
		Clause clause = new Clause(head, body);
		clauses.add(clause);

		PositiveAtom query = PositiveAtom.create(PredicateSym.create(ruleId, 0), emptyTermArray);
		return query;
	}

	public Set<PositiveAtom> query(PositiveAtom query) {
		if (!initialized) {
			throw new RuntimeException("must be initialized() before a query");
		}
		Set<PositiveAtom> bindings = abcDatalog.query(query);
		return bindings;
	}

	public Set<ConstOnlySubstitution> query(List<PositiveAtom> query) {
		if (!initialized) {
			throw new RuntimeException("must be initialized() before a query");
		}
		Set<ConstOnlySubstitution> bindings = abcDatalog.query(query);
		return bindings;
	}

	public int countQuerySolutions(List<PositiveAtom> query) {
		Set<ConstOnlySubstitution> bindings = query(query);
		return bindings.size();
	}

	public boolean isQueryTrue(List<PositiveAtom> query) {
		return countQuerySolutions(query) > 0;
	}

}
