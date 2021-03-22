package jcfgonc.blender.logic;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.githhub.aaronbembenek.querykb.Conjunct;
import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.KnowledgeBaseBuilder;
import com.githhub.aaronbembenek.querykb.Query;

import graph.StringEdge;
import graph.StringGraph;
import utils.VariousUtils;

/**
 * contains methods to easily interface between my code and Aaron's querykb tool
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class QueryKB {
	public static KnowledgeBase buildKnowledgeBase(StringGraph semanticGraph) {
		KnowledgeBaseBuilder kbb = new KnowledgeBaseBuilder();

		if (semanticGraph.isEmpty()) {
			System.err.println("buildKnowledgeBase(): given kbGraph is empty");
		}

		for (StringEdge edge : semanticGraph.edgeSet()) {
			String label = edge.getLabel();
			String source = edge.getSource();
			String target = edge.getTarget();
			kbb.addFact(label, source, target);
		}

		KnowledgeBase kb = kbb.build();
		return kb;
	}

	/**
	 * given a blend in KB form and a frame as a query, counts the occurrences of the frame in the blend using aaron's querykb tool and returns the
	 * logarithm of the occurrence.
	 * 
	 */
	public static double countFrameMatchesLog(KnowledgeBase blendKB, Query frameQuery, int blockSize, int parallelLimit, BigInteger solutionLimit,
			boolean reorder, Long timeout) {
		// 3. apply query to KnowledgeBase
		BigInteger matches = blendKB.count(frameQuery, blockSize, parallelLimit, solutionLimit, reorder, timeout);

		// matches will always be >= 0
		if (matches.compareTo(BigInteger.ZERO) < 0) {
			throw new RuntimeException("querykb count() returned " + matches);
		}

		if (matches.compareTo(BigInteger.ONE) < 0) { // zero matches
			return -1.0d;
		} else { // more than than one match
			return VariousUtils.log2(matches) / FastMath.log(2, 10);
		}
	}

	/**
	 * given a blend in KB form and a frame as a query, counts the occurrences of the frame in the blend using aaron's querykb tool. This function
	 * prevents negative values from being returned. The maximum number of occurrences is Integer.MAX_VALUE. The minimum is zero.
	 * 
	 */
	public static int countFrameMatchesInt(KnowledgeBase blendKB, Query frameQuery, int blockSize, int parallelLimit, BigInteger solutionLimit,
			boolean reorder, Long timeout) {
		// 3. apply query to KnowledgeBase
		int matches = blendKB.count(frameQuery, blockSize, parallelLimit, solutionLimit, reorder, timeout).intValue();

		if (matches < 0) // this means there was an overflow
			return Integer.MAX_VALUE;
		return matches;
	}
	
	/**
	 * creates a querykb query
	 * 
	 * @param graph
	 * @return
	 */
	public static Query createQueryFromStringGraph(final StringGraph graph) {
		return Query.make(createConjunctionFromStringGraph(graph, null));
	}

	/**
	 * creates a querykb conjunction to be used as a query using the given variable<=>variable mapping. If conceptToVariable is null then the edge's
	 * concepts names are used as variables instead of the mapping.
	 * 
	 * @param pattern
	 * @param conceptToVariable
	 * @return
	 */
	public static List<Conjunct> createConjunctionFromStringGraph(final StringGraph pattern, final HashMap<String, String> conceptToVariable) {
		ArrayList<Conjunct> conjunctList = new ArrayList<>();
		if (conceptToVariable != null) {
			// create the query as a conjunction of terms
			for (StringEdge edge : pattern.edgeSet()) {
				String edgeLabel = edge.getLabel();
				String source = conceptToVariable.get(edge.getSource());
				String target = conceptToVariable.get(edge.getTarget());
				conjunctList.add(Conjunct.make(edgeLabel, source, target));
			}
		} else {
			// create the query as a conjunction of terms
			for (StringEdge edge : pattern.edgeSet()) {
				String edgeLabel = edge.getLabel();
				String sourceVar = edge.getSource();
				String targetVar = edge.getTarget();
				conjunctList.add(Conjunct.make(edgeLabel, sourceVar, targetVar));
			}
		}
		return conjunctList;
	}

}
