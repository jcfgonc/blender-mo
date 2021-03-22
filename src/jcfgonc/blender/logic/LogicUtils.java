package jcfgonc.blender.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import graph.GraphAlgorithms;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import structures.Mapping;
import structures.UnorderedPair;
import utils.VariousUtils;

public class LogicUtils {

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

	public static double calculateInputSpacesBalance(StringGraph blendSpace, Mapping<String> mapping) {
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
					System.err.printf("leftConcepts does not contain the left part (%s) of the blended concept\n", left);
				}
				if (!rightConcepts.contains(right)) {
					System.err.printf("rightConcepts does not contain the right part (%s) of the blended concept\n", right);
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
		if (u > 0.999)
			System.lineSeparator();
		return u;
	}

	public static double[] calculateRelationStatistics(StringGraph blendSpace) {
		double[] stats = new double[3];
		Object2IntOpenHashMap<String> relHist = GraphAlgorithms.countRelations(blendSpace);
		int numRelations = relHist.size();
		double mean = 0;
		double stddev = 1.1; // higher value means there is a relation with a higher frequency than the other relations
		if (numRelations == 0) {
			throw new RuntimeException("empty relation histogram, unable to compute statistics");
		} else if (numRelations == 1) {
		} else {
			DescriptiveStatistics ds = GraphAlgorithms.getRelationStatisticsNormalized(relHist, blendSpace.numberOfEdges());
//			System.out.printf("%d\t%f\t%f\t%f\t%f\t%s\n", ds.getN(), ds.getMin(), ds.getMean(), ds.getMax(), ds.getStandardDeviation(),
//					relHist.toString());
			mean = ds.getMean(); // 0...1
			stddev = ds.getStandardDeviation();
		}
		stats[0] = mean;
		stats[1] = stddev;
		stats[2] = numRelations;
		return stats;
	}

	/**
	 * calculates mixing of nearby concepts belonging to different subsets (left/right) of the mapping
	 * 
	 * @param blendSpace
	 * @param mapping
	 * @return
	 */
	public static int calculateMappingMix(StringGraph blendSpace, Mapping<String> mapping) {
		int mix = 0;
		HashSet<UnorderedPair<String>> checkedPairs = new HashSet<UnorderedPair<String>>(16, 0.333f);
		Set<String> leftConcepts = mapping.getLeftConcepts();
		Set<String> rightConcepts = mapping.getRightConcepts();
		for (String concept0 : blendSpace.getVertexSet()) {
			Set<String> neighSet = blendSpace.getNeighborVertices(concept0);
			for (String concept1 : neighSet) {

				UnorderedPair<String> neighPar = new UnorderedPair<String>(concept0, concept1);
				if (checkedPairs.contains(neighPar))
					continue;
				checkedPairs.add(neighPar);

				if (isBlend(concept0)) { // blended concept0
					if (isBlend(concept1)) { // blended concept0 connected to another blended concept1
						mix++;
					} else { // blended concept0 connected to a normal concept1
						if (mapping.containsConcept(concept1)) { // concept1 present in the mapping?
							mix++;
						}
					}
				} else { // normal concept0
					if (isBlend(concept1)) { // normal concept0 connected to a blended concept1
						if (mapping.containsConcept(concept0)) { // concept0 present in the mapping?
							mix++;
						}
					} else { // normal concept0 connected to a normal concept1
						if (leftConcepts.contains(concept0) && rightConcepts.contains(concept1) || //
								leftConcepts.contains(concept1) && rightConcepts.contains(concept0)) {
							mix++;
						}
					}
				}
			}
		}
		return mix;
	}

	public static boolean isBlend(String concept) {
		return concept.indexOf('|') >= 0;
	}
}
