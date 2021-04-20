package jcfgonc.blender;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomAdaptor;

import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import jcfgonc.blender.structures.Blend;
import structures.ConceptPair;
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

		// mutateMappings(random, blend); // mappings are currently static

		if (MOEA_Config.EDGE_MUTATION_NUMBER_STEPS >= 2) {
			double r = Math.pow(random.nextDouble(), MOEA_Config.EDGE_MUTATION_PROBABILITY_POWER);
			int numberMutations = (int) Math.ceil(r * MOEA_Config.EDGE_MUTATION_NUMBER_STEPS);
			// ceil of 0 could happen
			if (numberMutations <= 1)
				numberMutations = 1;
			for (int i = 0; i < numberMutations; i++) {
				mutateBlendSpace(blend);
			}
		} else {
			mutateBlendSpace(blend);
		}
	}

	private static void mutateBlendSpace(Blend blend) {
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
	 * adds a random edge from the input space to the blend space using the given referenceConcept<br>
	 * TESTED, SEEMS OK
	 * 
	 * @param blendSpace
	 * @param inputSpace
	 * @param random
	 * @param concept
	 * @return true if a new edge was added, false otherwise (because it could not)
	 */
	private static boolean addNeighbourEdge(StringGraph blendSpace, String concept) {
		if (concept == null) {
			concept = VariousUtils.getRandomElementFromCollection(inputSpaceVerticesList, random);
		}

		if (concept.indexOf('|') < 0) { // not a blended concept
			// get neighbor edges touching the concept in the input space
			// subtract from those edges the ones already existing in the blend space
			Set<StringEdge> newEdges = VariousUtils.subtract(inputSpace.edgesOf(concept), blendSpace.edgeSet(), false);
			if (newEdges.isEmpty()) {
				return false;
			}
			// try adding one of the touching edges
			ArrayList<StringEdge> shuffledEdges = VariousUtils.asShuffledArray(newEdges, random);
			for (StringEdge edgeToAdd : shuffledEdges) {
				if (blendSpace.addEdge(edgeToAdd)) {
					return true;
				}
			}
		} else { // the concept is a blended concept
			// get touching edges from both components
			String[] concepts = VariousUtils.fastSplit(concept, '|');
			String a = concepts[0];
			String b = concepts[1];
			// get neighbor edges touching the concept in the input space
			Set<StringEdge> edges_a = inputSpace.edgesOf(a);
			Set<StringEdge> edges_b = inputSpace.edgesOf(b);
			if (edges_a.isEmpty()) {
				System.err.printf("122: concept %s involved in mapping %s has no edges\n", a, concept);
				// return false;
			}
			if (edges_b.isEmpty()) {
				System.err.printf("126: concept %s involved in mapping %s has no edges\n", b, concept);
				// return false;
			}
			Set<StringEdge> ec = VariousUtils.mergeSets(edges_a, edges_b);
			if (ec.isEmpty()) {
				System.err.println("concept " + concept + " has no connected edges");
				return false;
			}
			// subtract from those edges the ones already existing in the blend space
			Set<StringEdge> newEdges = VariousUtils.subtract(ec, blendSpace.edgeSet(), false);
			if (newEdges.isEmpty()) {
				return false;
			}
			// try adding one of the touching edges
			ArrayList<StringEdge> shuffledEdges = VariousUtils.asShuffledArray(newEdges, random);
			for (StringEdge edge : shuffledEdges) {
				StringEdge edgeToAdd = edge.replaceSourceOrTarget(a, concept).replaceSourceOrTarget(b, concept);
				// these two lines are to maintain the existence of the blended concept which does not exist in the input space
				if (blendSpace.addEdge(edgeToAdd)) {
					return true;
				}
			}
		}
		return false; // could not add an edge
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
	private static boolean addMappingEdge(StringGraph blendSpace, Mapping<String> mapping, String referenceConcept) {
		if (referenceConcept.contains("|")) {
			// try to connect the given blended concept to a concept pair existing in the mapping
			String[] concepts = VariousUtils.fastSplit(referenceConcept, '|');
			// get concept edges and subtract from them the ones already existing in the blend space
			String a = concepts[0];
			String b = concepts[1];
			Set<StringEdge> edges_a = inputSpace.edgesOf(a);
			Set<StringEdge> edges_b = inputSpace.edgesOf(b);
			if (edges_a.isEmpty()) {
				System.err.printf("171: concept %s involved in mapping %s has no edges\n", a, referenceConcept);
				return false;
			}
			if (edges_b.isEmpty()) {
				System.err.printf("175: concept %s involved in mapping %s has no edges\n", b, referenceConcept);
				return false;
			}
			edges_a = VariousUtils.subtract(edges_a, blendSpace.edgeSet(), false);
			// edges_b = VariousUtils.subtract(edges_b, blendSpace.edgeSet(), false); // unused because of the below getEdgesConnecting()
			edges_b = null; // unused variable, free memory

			for (StringEdge edge_a : VariousUtils.asShuffledArray(edges_a, random)) {
				String c = edge_a.getOppositeOf(a);
				ConceptPair<String> conceptPair = mapping.getConceptPair(c);
				if (conceptPair == null)
					continue;
				String d = conceptPair.getOpposingConcept(c);
				String relation = edge_a.getLabel();
				if (edge_a.outgoesFrom(a)) {
					Set<StringEdge> edges_b_filtered = VariousUtils.subtract(inputSpace.getEdgesConnecting(b, d, relation), blendSpace.edgeSet(),
							false);
					if (edges_b_filtered.isEmpty())
						continue;
				} else {
					Set<StringEdge> edges_b_filtered = VariousUtils.subtract(inputSpace.getEdgesConnecting(d, b, relation), blendSpace.edgeSet(),
							false);
					if (edges_b_filtered.isEmpty())
						continue;
				}
				StringEdge edgeToAdd = edge_a.replaceSourceOrTarget(a, referenceConcept);
				edgeToAdd = edgeToAdd.replaceSourceOrTarget(c, conceptPair.toString());
				if (blendSpace.addEdge(edgeToAdd)) {
			//		LogicUtils.calculateUnpacking(blendSpace, mapping);
					return true; // done, one edge added
				}
				return false;
			}

		} else { // CODE CHECKED, SEEMS OK
			// try to connect the given normal concept to a concept pair existing in the mapping
			// subtract from those edges the ones already existing in the blend space
			String c = referenceConcept;
			Set<StringEdge> edges = VariousUtils.subtract(inputSpace.edgesOf(c), blendSpace.edgeSet(), false);
			if (edges.isEmpty()) {
				return false;
			}
			// see if the edge connects to a concept involved in the mapping
			for (StringEdge edge : VariousUtils.asShuffledArray(edges, random)) {
				String a = edge.getOppositeOf(c);
				if (mapping.containsConcept(a)) { // other concept is involved in the mapping
					// get the involved concept pair
					ConceptPair<String> conceptPair = mapping.getConceptPair(a);
					// recreate the edge renaming the other concept to a concept pair blend
					StringEdge edgeToAdd = edge.replaceSourceOrTarget(a, conceptPair.toString());
					if (blendSpace.addEdge(edgeToAdd)) {
				//		LogicUtils.calculateUnpacking(blendSpace, mapping);
						return true; // done, one edge added
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
		// either add a random edge using the mapping or not
		if (blendSpace.isEmpty()) {
			if (random.nextBoolean()) { // use the mapping
				// get a random pair from the mapping
				ConceptPair<String> conceptPair = mapping.getRandomPair(random);
				addFirstMappingEdge(blendSpace, conceptPair);
			} else { // OR do not use the mapping
				addNeighbourEdge(blendSpace, null);
			}
		} else {
			if (random.nextBoolean()) { // use the mapping
				// iterate *randomly* through the blend searching for insertable edges connecting one concept/blended concept to a concept pair
				ArrayList<String> blendConcepts = VariousUtils.asShuffledArray(blendSpace.getVertexSet(), random);
				// iterate trough the blend space's concepts
				for (String referenceConcept : blendConcepts) { // try to add something
					// try to connect that concept to a mapping pair
					boolean added = addMappingEdge(blendSpace, mapping, referenceConcept);
					if (added) {
						break;
					}
				}
			} else { // OR do not use the mapping
				ArrayList<String> shuffledConcepts = VariousUtils.asShuffledArray(blendSpace.getVertexSet(), random);
				// randomly try to add an edge
				// it may fail - if so, try again
				for (String referenceConcept : shuffledConcepts) {
					boolean added = addNeighbourEdge(blendSpace, referenceConcept);
					if (added) {
						break;
					}
				}
			}
		}
	}

	// OK, TESTED
	private static boolean addFirstMappingEdge(StringGraph blendSpace, ConceptPair<String> conceptPair) {
		// first get all edges touching the left and right concepts in the pair
		String a = conceptPair.getLeftConcept();
		String b = conceptPair.getRightConcept();
		Set<StringEdge> newEdges = VariousUtils.mergeSets(inputSpace.edgesOf(a), inputSpace.edgesOf(b));
		if (newEdges.isEmpty()) {
			return false;
		}
		String blend = a + "|" + b;
		ArrayList<StringEdge> shuffledEdges = VariousUtils.asShuffledArray(newEdges, random);
		for (StringEdge edge : shuffledEdges) {
			// add one of the touching edges, replacing left/right concepts with the blended pair
			StringEdge edgeToAdd = edge.replaceSourceOrTarget(a, blend).replaceSourceOrTarget(b, blend);
			if (blendSpace.addEdge(edgeToAdd)) {
				return true;
			}
		}
		return false;
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
		// remove smaller components if more than one
		GraphAlgorithms.removeSmallerComponents(blendSpace);
	}
}
