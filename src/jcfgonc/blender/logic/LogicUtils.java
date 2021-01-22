package jcfgonc.blender.logic;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;

import com.githhub.aaronbembenek.querykb.Conjunct;
import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.KnowledgeBaseBuilder;
import com.githhub.aaronbembenek.querykb.Query;

import graph.GraphAlgorithms;
import graph.GraphReadWrite;
import graph.StringEdge;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import structures.CSVReader;
import structures.ListOfSet;
import structures.Mapping;
import utils.VariousUtils;

public class LogicUtils {

	/**
	 * the stringraph's vertices must be of the form text0|text1
	 * 
	 * @param sg
	 * @param mapping
	 */
	public static void addStringGraphVerticesToMapping(StringGraph sg, Mapping<String> mapping) {
		for (String vertice : sg.getVertexSet()) {
			String[] tokens = VariousUtils.fastSplit(vertice, '|');
			if (tokens.length == 2) {
				mapping.add(tokens[0], tokens[1]);
			}
		}
	}

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
	 * Calculates and returns the relation histogram
	 * 
	 * @param relationToCountMap a map of string (the relation) to integer (relation count), i.e, the absolute frequency table
	 * @return
	 */
	public static DescriptiveStatistics calculateRelationHistogram(Object2IntOpenHashMap<String> relationToCountMap) {
		int size = relationToCountMap.size();
		if (size == 0) {
			return null;
		}
		double[] count_d = new double[size];
		int i = 0;
		for (String key : relationToCountMap.keySet()) {
			count_d[i++] = relationToCountMap.getInt(key);
		}
		DescriptiveStatistics ds = new DescriptiveStatistics(count_d);
		return ds;
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
			return log2(matches) / FastMath.log(2, 10);
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
	 * creates a mapping from each element of the given concept set to a unique and consecutive variable named Xi, i in 0...set size.
	 * 
	 * @param pattern
	 * @return
	 */
	public static HashMap<String, String> createConceptToVariableMapping(Set<String> vertexSet) {
		HashMap<String, String> conceptToVariable = new HashMap<>(vertexSet.size() * 2);
		int varCounter = 0;
		for (String concept : vertexSet) {
			String varName = "X" + varCounter;
			conceptToVariable.put(concept, varName);
			varCounter++;
		}
		return conceptToVariable;
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

	/**
	 * generates a graph with using the variables' map instead of the original concepts. Used to convert a specific graph to a frame.
	 * 
	 * @param graph
	 * @param conceptToVariable
	 * @return
	 */
	public static StringGraph createPatternWithVars(StringGraph graph, HashMap<String, String> conceptToVariable) {
		StringGraph patternWithVars = new StringGraph();

		// create the query as a conjunction of terms
		for (StringEdge edge : graph.edgeSet()) {

			String edgeLabel = edge.getLabel();
			String sourceVar = edge.getSource();
			String targetVar = edge.getTarget();
			if (conceptToVariable != null) {
				sourceVar = conceptToVariable.get(sourceVar);
				targetVar = conceptToVariable.get(targetVar);
			}
			patternWithVars.addEdge(sourceVar, targetVar, edgeLabel);
		}

		if (graph.numberOfEdges() <= 1) {
			throw new RuntimeException();
		}

		// bug check
		if (patternWithVars.numberOfEdges() != graph.numberOfEdges()) {
			throw new RuntimeException();
		}

		return patternWithVars;
	}

	/**
	 * This function calculates base 2 log because finding the number of occupied bits is trivial.
	 * 
	 * @param val
	 * @return
	 */
	private static double log2(BigInteger val) {
		// from https://stackoverflow.com/a/9125512 by Mark Jeronimus
		// ---
		// Get the minimum number of bits necessary to hold this value.
		int n = val.bitLength();

		// Calculate the double-precision fraction of this number; as if the
		// binary point was left of the most significant '1' bit.
		// (Get the most significant 53 bits and divide by 2^53)
		long mask = 1L << 52; // mantissa is 53 bits (including hidden bit)
		long mantissa = 0;
		int j = 0;
		for (int i = 1; i < 54; i++) {
			j = n - i;
			if (j < 0)
				break;

			if (val.testBit(j))
				mantissa |= mask;
			mask >>>= 1;
		}
		// Round up if next bit is 1.
		if (j > 0 && val.testBit(j - 1))
			mantissa++;

		double f = mantissa / (double) (1L << 52);

		// Add the logarithm to the number of bits, and subtract 1 because the
		// number of bits is always higher than necessary for a number
		// (ie. log2(val)<n for every val).
		return (n - 1 + Math.log(f) * 1.44269504088896340735992468100189213742664595415298D);
		// Magic number converts from base e to base 2 before adding. For other
		// bases, correct the result, NOT this number!
	}

	/**
	 * Check for components and leave only one, done IN-PLACE. If random is not null, select a random component to be the whole graph. Otherwise
	 * select the largest component.
	 * 
	 * @param random
	 * @param genes
	 * @return
	 * @return the components
	 */
	public static StringGraph removeAdditionalComponents(StringGraph pattern, RandomGenerator random) {
		// calculate components (sets of vertices)
		ListOfSet<String> components = GraphAlgorithms.extractGraphComponents(pattern);

		if (components.size() == 1) {
			HashSet<String> component0 = components.getSetAt(0);
			if (component0.isEmpty()) {
				System.err.println("### got an empty component");
			}
			return pattern; // break, no need to filter the pattern (ie remove additional components)
		}

		System.err.format("### got a pattern with %d components: %s\n", components.size(), components.toString());

		HashSet<String> largestComponent;
		if (random == null) {
			largestComponent = components.getSetAt(0);
		} else {
			largestComponent = components.getRandomSet(random);
		}

		// filter pattern with mask and store it back
		return new StringGraph(pattern, largestComponent);
	}

	public static List<StringGraph> readPatternResultsDelimitedFile(File file, String columnSeparator, boolean fileHasHeader, int graphColumnID)
			throws IOException {
		System.out.println("loading StringGraph CSV File " + file.getName());
		CSVReader csvData = CSVReader.readCSV(columnSeparator, file, fileHasHeader);
		int nFrames = csvData.getNumberOfRows();
		StringGraph[] frames = new StringGraph[nFrames];
		ArrayList<ArrayList<String>> csvRows = csvData.getRows();

		IntStream.range(0, nFrames).parallel().forEach(id -> { // parallelize conversion
			try {
				ArrayList<String> row = csvRows.get(id);
				String graphAsString = row.get(graphColumnID);
				frames[id] = GraphReadWrite.readCSVFromString(graphAsString);
			} catch (NoSuchFileException e) {
			} catch (IOException e) {
			}
		});
		return Arrays.asList(frames);
	}

	public static int countMappings(StringGraph blendSpace) {
		int numberMappings = 0;
		for (String vertex : blendSpace.getVertexSet()) {
			if (vertex.indexOf('|') >= 0) {
				numberMappings++;
			}
		}
		return numberMappings;
	}

	public static double evaluateVitalRelations(StringGraph blendSpace, Object2DoubleOpenHashMap<String> vitalRelations) {
		// histogram of blend relations
		Object2IntOpenHashMap<String> relHist = GraphAlgorithms.countRelations(blendSpace);

		// multiply-accumulate
		double sum = 0;
		for (Object2IntMap.Entry<String> entry : relHist.object2IntEntrySet()) {
			String relation = entry.getKey();
			int occurrences = entry.getIntValue();
			double weight = vitalRelations.getDouble(relation);
			sum += occurrences * weight;
		}
		double mean = sum / (double) blendSpace.numberOfEdges();
		return mean;
	}

	/**
	 * returns the blended concept (if existing) containing the given text. The concept will be a blend of the form *|text OR text|*
	 * 
	 * @param blendSpace
	 * @param text
	 * @return
	 */
	public static String getBlendContainingConcept(StringGraph blendSpace, String text) {
		for (String vertex : blendSpace.getVertexSet()) {
			if (vertex.indexOf('|') < 0)
				continue;
			String[] vertexTokens = VariousUtils.fastSplit(vertex, '|');
			for (String token : vertexTokens) {
				if (token.equals(text)) {
					return vertex;
				}
			}
		}
		return null;
	}

	public static double calculateUnpacking(StringGraph blendSpace, Mapping<String> mapping) {
		Set<String> leftConcepts = mapping.getLeftConcepts();
		Set<String> rightConcepts = mapping.getRightConcepts();
		int leftCount = 0;
		int rightCount = 0;
		for (String concept : blendSpace.getVertexSet()) {
			if (concept.indexOf('|') >= 0) { // blended concept
				String[] concepts = VariousUtils.fastSplit(concept, '|');
				String left = concepts[0];
				String right = concepts[1];
				// debug: this could happen if the blended concept is of the sort right|left instead of left|right
				if (!leftConcepts.contains(left)) {
					System.err.println("leftConcepts does not contain the left part of the blended concept");
				}
				if (!rightConcepts.contains(right)) {
					System.err.println("rightConcepts does not contain the right part of the blended concept");
				}
				leftCount += blendSpace.degreeOf(concept);
				rightCount += blendSpace.degreeOf(concept);
			} else {
				if (leftConcepts.contains(concept)) { // used in the mapping as a left concept
					leftCount += blendSpace.degreeOf(concept);
				} else if (leftConcepts.contains(concept)) {// used in the mapping as a right concept
					rightCount += blendSpace.degreeOf(concept);
				} else { // not referenced in the mapping

				}
			}
		}
		if (leftCount == 0 || rightCount == 0) {
			return 0;
		}
		double u = (double) Math.min(leftCount, rightCount) / Math.max(leftCount, rightCount);
		return u;
	}
}
