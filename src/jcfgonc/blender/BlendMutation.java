package jcfgonc.blender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomAdaptor;

import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import jcfgonc.blender.structures.Blend;
import structures.ConceptPair;
import structures.ListOfSet;
import structures.Mapping;
import utils.VariousUtils;

public class BlendMutation {
	private static StringGraph inputSpace;
	private static List<String> inputSpaceVerticesList;
	private static RandomAdaptor random;

	public static void setInputSpace(StringGraph inputSpace_) {
		BlendMutation.inputSpace = inputSpace_;
		BlendMutation.inputSpaceVerticesList = new ArrayList<String>(inputSpace_.getVertexSet());
	}

	public static void setRandom(RandomAdaptor random_) {
		BlendMutation.random = random_;
	}

	public static void mutateBlend(Blend blend) {

		if (BlenderMoConfig.EDGE_MUTATION_NUMBER_STEPS >= 2) {
			double r = Math.pow(random.nextDouble(), BlenderMoConfig.EDGE_MUTATION_PROBABILITY_POWER);
			int numberMutations = (int) Math.ceil(r * BlenderMoConfig.EDGE_MUTATION_NUMBER_STEPS);
			// ceil of 0 could happen
			if (numberMutations <= 1)
				numberMutations = 1;
			for (int i = 0; i < numberMutations; i++) {
				mutateEdges(blend);
			}
		} else {
			mutateEdges(blend);
		}

		// TODO!!! test if any concept of a mapping pair is present separately in the blend
		// mutateMappings(random, blend); // mappings are currently static

		// TODO remove smaller components if more than one
		ListOfSet<String> components = GraphAlgorithms.extractGraphComponents(blend.getBlendSpace());
		if (components.size() != 1) {
			System.err.println("graph components: " + components);
			// System.exit(0);
		}

	}

	private static void mutateEdges(Blend blend) {
		StringGraph blendSpace = blend.getBlendSpace();
		Mapping<String> mapping = blend.getMapping();
		int numEdges = blendSpace.numberOfEdges();

		if (numEdges == 0) { // no edge -> always add one edge
			addRandomEdge(blendSpace, mapping);

		} else if (numEdges == 1) { // one edge -> add another edge with the possibility of clearing the blend before

			if (random.nextBoolean()) { // remove the existing edge if random wishes so
				blendSpace.clear();
			}
			addRandomEdge(blendSpace, mapping);

		} else { // two or more edges -> add another edge OR remove one of the existing
			if (random.nextBoolean()) {
				addRandomEdge(blendSpace, mapping);

			} else {
				removeRandomEdge(blendSpace);
			}
		}
	}

	/**
	 * adds a random edge from the input space to the blend space using the existing blend's concept if possible <br>
	 * TESTED, SEEMS OK
	 * 
	 * @param blendSpace
	 * @param inputSpace
	 * @param random
	 * @param referenceConcept
	 * @return true if a new edge was added, false otherwise (because it could not)
	 */
	private static boolean addRandomNeighbourEdge(StringGraph blendSpace, String referenceConcept) {
		// get neighbor edges touching the concept in the input space
		// check if the concept is a blend or not
		if (referenceConcept.contains("|")) {// the concept is a blended concept, get touching edges from both components
			String[] concepts = referenceConcept.split("\\|");
			String c0 = concepts[0];
			String c1 = concepts[1];
			Set<StringEdge> ec = VariousUtils.mergeSets(inputSpace.edgesOf(c0), inputSpace.edgesOf(c1));
			// subtract from those edges the ones already existing in the blend space
			Set<StringEdge> newEdges = VariousUtils.subtract(ec, blendSpace.edgeSet(), true);
			if (newEdges.isEmpty()) {
				return false;
			}

			// try adding one of the touching edges
			ArrayList<StringEdge> shuffledEdges = new ArrayList<>(newEdges);
			Collections.shuffle(shuffledEdges, random);
			for (StringEdge edgeToAdd : shuffledEdges) {
//				if (blendSpace.anyEdgeConnectsUndirected(edgeToAdd.getSource(), edgeToAdd.getTarget()))
//					continue;
				// prevent symmetrical edges
				if (blendSpace.containsEdge(edgeToAdd.reverse())) {
					continue;
				}

				// these two lines are to maintain the existence of the blended concept which does not exist in the input space
				edgeToAdd = edgeToAdd.replaceSourceOrTarget(c0, referenceConcept); // replace 'a' with 'a|b'
				edgeToAdd = edgeToAdd.replaceSourceOrTarget(c1, referenceConcept); // replace 'b' with 'a|b'

				if (edgeToAdd.getSource().equals(edgeToAdd.getTarget()))
					continue;
				if (blendSpace.containsEdge(edgeToAdd.reverse()))
					continue;
				// add the new edge
				blendSpace.addEdge(edgeToAdd);
				return true;
			}

		} else { // not a blended concept
			Set<StringEdge> ec = inputSpace.edgesOf(referenceConcept);
			// subtract from those edges the ones already existing in the blend space
			Set<StringEdge> newEdges = VariousUtils.subtract(ec, blendSpace.edgeSet(), true);
			if (newEdges.isEmpty()) {
				return false;
			}
			// try adding one of the touching edges
			ArrayList<StringEdge> shuffledEdges = new ArrayList<>(newEdges);
			Collections.shuffle(shuffledEdges, random);
			for (StringEdge edgeToAdd : shuffledEdges) {
//				if (blendSpace.anyEdgeConnectsUndirected(edgeToAdd.getSource(), edgeToAdd.getTarget()))
//					continue;
				// prevent symmetrical edges
				if (blendSpace.containsEdge(edgeToAdd.reverse())) {
					continue;
				}
				if (edgeToAdd.getSource().equals(edgeToAdd.getTarget()))
					continue;
				if (blendSpace.containsEdge(edgeToAdd.reverse()))
					continue;
				// add the new edge
				blendSpace.addEdge(edgeToAdd);
				return true;
			}
		}
		return false;
	}

	/**
	 * adds a random edge using a concept pair from the input space to the blend space
	 * 
	 * @param blendSpace
	 * @param inputSpace
	 * @param random
	 * @return
	 * @return true if a new edge was added, false otherwise (because it could not)
	 */
	private static boolean addRandomNeighbourEdgeUsingMapping(StringGraph blendSpace, Mapping<String> mapping) {
		Set<String> blendVertexSet = blendSpace.getVertexSet();
		if (blendVertexSet.isEmpty()) { // if the blend space is empty, get an edge touching the left OR right concepts of a pair
			// this block seems OK
			ConceptPair<String> pair = mapping.getRandomPair(random);
			// first get all edges touching the left and right concepts in the pair
			String left = pair.getLeftConcept();
			String right = pair.getRightConcept();
			Set<StringEdge> ec = VariousUtils.mergeSets(inputSpace.edgesOf(left), inputSpace.edgesOf(right));
			// subtract from those edges the ones already existing in the blend space
			Set<StringEdge> newEdges = VariousUtils.subtract(ec, blendSpace.edgeSet(), true);
			if (newEdges.isEmpty()) {
				return false;
			}

			String blendedPair = left + "|" + right;
			ArrayList<StringEdge> shuffledEdges = new ArrayList<>(newEdges);
			Collections.shuffle(shuffledEdges, random);
			for (StringEdge edgeToAdd : shuffledEdges) {
				// add one of the touching edges, replacing left/right concepts with the blended pair
				// replace original left/right concepts with the blended concept
				edgeToAdd = edgeToAdd.replaceSourceOrTarget(left, blendedPair).replaceSourceOrTarget(right, blendedPair);
//				if (blendSpace.anyEdgeConnectsUndirected(edgeToAdd.getSource(), edgeToAdd.getTarget()))
//					continue;
				// prevent symmetrical edges
				if (blendSpace.containsEdge(edgeToAdd.reverse())) {
					continue;
				}
				if (edgeToAdd.getSource().equals(edgeToAdd.getTarget()))
					continue;
				if (blendSpace.containsEdge(edgeToAdd.reverse()))
					continue;
				blendSpace.addEdge(edgeToAdd);
				return true;
			}

		} else {
			// iterate *randomly* through the blend searching for insertable edges connecting one concept/blended concept to a concept pair
			ArrayList<String> blendConcepts = new ArrayList<>(blendSpace.getVertexSet());
			Collections.shuffle(blendConcepts, random);
			for (String concept : blendConcepts) { // try to add something
				if (concept.contains("|")) {
					// this block seems OK
					String[] concepts = concept.split("\\|");
					Set<StringEdge> ec = VariousUtils.mergeSets(inputSpace.edgesOf(concepts[0]), inputSpace.edgesOf(concepts[1]));
					// subtract from those edges the ones already existing in the blend space
					Set<StringEdge> fEdges = VariousUtils.subtract(ec, blendSpace.edgeSet(), true);
					if (fEdges.isEmpty()) {
						continue;
					}
					ArrayList<StringEdge> shuffledEdges = new ArrayList<>(fEdges);
					Collections.shuffle(shuffledEdges, random);
					// see if in the input space the edge connects to a concept involved in the mapping
					for (StringEdge edge : shuffledEdges) {
						// if blended concept contains both the edge's concepts the code below would make a self-looping edge, so skip it
						Set<String> conceptElementsSet = Set.of(VariousUtils.fastSplit(concept, '|'));
						if (conceptElementsSet.contains(edge.getSource()) && conceptElementsSet.contains(edge.getTarget())) {
							continue;
						}

						// concept is a blend, check opposites of both parts in the input space
						for (String conceptPart : concepts) {
							String otherConcept = edge.getOppositeOf(conceptPart);
							// see if the other concept is involved in the mapping
							if (otherConcept != null & mapping.containsConcept(otherConcept)) {
								// get the involved concept pair
								ConceptPair<String> conceptPair = mapping.getConceptPair(otherConcept);
								// recreate the edge by reconnecting the new concept pair to the existing blend concept
								String blendedPair = conceptPair.getLeftConcept() + "|" + conceptPair.getRightConcept();
								StringEdge edgeToAdd0 = edge.replaceSourceOrTarget(otherConcept, blendedPair);
								StringEdge edgeToAdd1 = edgeToAdd0.replaceSourceOrTarget(conceptPart, concept);
//								if (blendSpace.anyEdgeConnectsUndirected(edgeToAdd1.getSource(), edgeToAdd1.getTarget())) {
//									continue;
//								}
								// prevent symmetrical edges
								if (blendSpace.containsEdge(edgeToAdd1.reverse())) {
									continue;
								}
								// prevent self-loops
								if (edgeToAdd1.getSource().equals(edgeToAdd1.getTarget())) {
									continue;
								}
								if (blendSpace.containsEdge(edgeToAdd1.reverse()))
									continue;
								blendSpace.addEdge(edgeToAdd1);
								return true; // done, one edge added
							}
						}
					}

				} else {
					// this block seems OK
					// iterate *randomly* through each connected edge in the input space
					// subtract from those edges the ones already existing in the blend space
					Set<StringEdge> ec = inputSpace.edgesOf(concept);
					Set<StringEdge> fEdges = VariousUtils.subtract(ec, blendSpace.edgeSet(), true);
					if (fEdges.isEmpty()) {
						continue;
					}
					ArrayList<StringEdge> shuffledEdges = new ArrayList<>(fEdges);
					Collections.shuffle(shuffledEdges, random);
					// see if the edge connects to a concept involved in the mapping
					for (StringEdge edge : shuffledEdges) {
						String otherConcept = edge.getOppositeOf(concept);
						if (mapping.containsConcept(otherConcept)) { // other concept is involved in the mapping
							// get the involved concept pair
							ConceptPair<String> conceptPair = mapping.getConceptPair(otherConcept);
							// recreate the edge renaming the other concept to a concept pair blend
							String blendedPair = conceptPair.getLeftConcept() + "|" + conceptPair.getRightConcept();
							StringEdge edgeToAdd = edge.replaceSourceOrTarget(otherConcept, blendedPair);
//							if (blendSpace.anyEdgeConnectsUndirected(edgeToAdd.getSource(), edgeToAdd.getTarget()))
//								continue;
							// prevent symmetrical edges
							if (blendSpace.containsEdge(edgeToAdd.reverse())) {
								continue;
							}
							if (blendSpace.containsEdge(edgeToAdd.reverse()))
								continue;
							blendSpace.addEdge(edgeToAdd);
							return true; // done, one edge added
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * returns a set of concept pairs whose pairs (in the mapping) contain at least one of the given concepts in the mask
	 * 
	 * @param mask    list of concepts to search in the mapping
	 * @param mapping a mapping containing concept pairs
	 * @return
	 */
	@SuppressWarnings("unused")
	private static HashSet<ConceptPair<String>> getConceptPairsContainingConcepts(Set<String> mask, Mapping<String> mapping) {
		HashSet<ConceptPair<String>> pairs = new HashSet<ConceptPair<String>>();
		for (String concept : mask) {
			// get the pair containing the concept
			ConceptPair<String> pair = mapping.getConceptPair(concept);
			if (pair != null) {
				pairs.add(pair);
			}
		}
		return pairs;
	}

	private static void addRandomEdge(StringGraph blendSpace, Mapping<String> mapping) {
		boolean added = false;
		int tries = 0;
		// either add a random edge using the mapping or not
		while (true) {
			if (random.nextBoolean()) { // use the mapping
				added = addRandomNeighbourEdgeUsingMapping(blendSpace, mapping);
			} else { // OR do not use the mapping
						// randomly try to add an edge, it may fail - if so, try again
						// get a random concept from the blend space (if not empty) or from the input space (otherwise)
				Set<String> blendVertexSet = blendSpace.getVertexSet();
				String referenceConcept;
				if (blendVertexSet.isEmpty()) {
					referenceConcept = VariousUtils.getRandomElementFromCollection(inputSpaceVerticesList, random);
				} else {
					referenceConcept = VariousUtils.getRandomElementFromCollection(blendVertexSet, random);
					// System.out.println("chosen " + referenceConcept + " from " + blendVertexSet);
				}
				added = addRandomNeighbourEdge(blendSpace, referenceConcept);
			}
			if (added)
				break;
			if (tries >= 10)
				break;
			// another round
			tries++;
		}

//		if (!added) {
		// System.out.println("failed to add an edge");
		// }
	}

	/**
	 * removes randomly one edge from the lowest degree vertex within the blend space.
	 * 
	 * @param random
	 * @param blendSpace
	 */
	private static void removeRandomEdge(StringGraph blendSpace) {
		// get the vertex with the lowest degree
		String lowestDegreeVertex = GraphAlgorithms.getLowestDegreeVertex(blendSpace.getVertexSet(), blendSpace);
		// remove a random edge from that vertex
		Set<StringEdge> edgesOfLowest = blendSpace.edgesOf(lowestDegreeVertex);
		StringEdge edgeToDelete = VariousUtils.getRandomElementFromCollection(edgesOfLowest, random);
		blendSpace.removeEdge(edgeToDelete);
	}

}
