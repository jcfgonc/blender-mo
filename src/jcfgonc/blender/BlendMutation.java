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
		} else if (numEdges == 1) { // one edge -> add another edge with the possibility of clearing the blend before (just an optimization)
			if (random.nextBoolean()) { // remove the existing edge if random wishes so
				blendSpace.clear();
			}
			addRandomEdge(blendSpace, mapping); // make sure the blend has at least one edge
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

		boolean conceptIsNormal = concept.indexOf('|') < 0;
		if (conceptIsNormal) { // not a blended concept
			// get neighbor edges touching the concept in the input space
			// subtract from those edges the ones already existing in the blend space
			Set<StringEdge> newEdges = VariousUtils.subtract(inputSpace.edgesOf(concept), blendSpace.edgeSet());
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
				System.err.printf("concept %s involved in mapping %s has no edges\n", a, concept);
			}
			if (edges_b.isEmpty()) {
				System.err.printf("concept %s involved in mapping %s has no edges\n", b, concept);
			}
			Set<StringEdge> ec = VariousUtils.mergeSets(edges_a, edges_b);
			if (ec.isEmpty()) {
				System.err.println("concept " + concept + " has no connected edges");
				return false;
			}
			// subtract from those edges the ones already existing in the blend space
			Set<StringEdge> newEdges = VariousUtils.subtract(ec, blendSpace.edgeSet());
			if (newEdges.isEmpty()) {
				return false;
			}
			// try adding one of the touching edges
			ArrayList<StringEdge> shuffledEdges = VariousUtils.asShuffledArray(newEdges, random);
			for (StringEdge edge : shuffledEdges) {
				StringEdge edgeToAdd = edge.replaceSourceOrTarget(a, concept).replaceSourceOrTarget(b, concept);
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
		if (referenceConcept.contains("|")) { // checked, OK
			// try to connect the given blended concept to a concept pair existing in the mapping
			String[] concepts = VariousUtils.fastSplit(referenceConcept, '|');
			// get concept edges and subtract from them the ones already existing in the blend space
			String a = concepts[0];
			String b = concepts[1];
			Set<StringEdge> edges_a = inputSpace.edgesOf(a);
			Set<StringEdge> edges_b = inputSpace.edgesOf(b);
			if (edges_a.isEmpty()) {
				System.err.printf("concept %s involved in mapping %s has no edges\n", a, referenceConcept);
				return false;
			}
			if (edges_b.isEmpty()) {
				System.err.printf("concept %s involved in mapping %s has no edges\n", b, referenceConcept);
				return false;
			}
			Set<StringEdge> newEdges = VariousUtils.mergeSets(edges_a, edges_b);
			newEdges = VariousUtils.subtract(newEdges, blendSpace.edgeSet());
			edges_a = null; // unused variable, free memory
			edges_b = null; // unused variable, free memory

			ArrayList<StringEdge> shuffledNewEdges = VariousUtils.asShuffledArray(newEdges, random);

			for (StringEdge newEdge : shuffledNewEdges) {
				String c = newEdge.getOppositeOf(a);
				ConceptPair<String> conceptPair = mapping.getConceptPair(c);
				if (conceptPair == null)
					continue;
				String d = conceptPair.getOpposingConcept(c);
				// a-c and b-d edges must have the same direction and relation
				String relation = newEdge.getLabel();
				// check if it exists an edge b->d using relation
				if (newEdge.outgoesFrom(a)) {
					if (!inputSpace.containsEdge(b, d, relation))
						continue;
				} else { // OR if it exists an edge d->b using relation
					if (!inputSpace.containsEdge(d, b, relation))
						continue;
				}
				StringEdge edgeToAdd = newEdge.replaceSourceOrTarget(a, referenceConcept); // correct

				// recreate the edge renaming the concept 'c' to 'c', 'd' OR 'c|'d
				String targetRename = null;
				switch (random.nextInt(3)) {
				case 0:
					targetRename = c;
					break;
				case 1:
					targetRename = d;
					break;
				case 2:
					targetRename = conceptPair.toString();
					break;
				}
				edgeToAdd = edgeToAdd.replaceSourceOrTarget(c, targetRename);
				if (blendSpace.addEdge(edgeToAdd)) {
					return true;
				}
				return false;
			}

		} else { // checked, OK
			// try to connect the given normal concept to a concept pair existing in the mapping
			// subtract from those edges the ones already existing in the blend space
			String c = referenceConcept;
			Set<StringEdge> edges = VariousUtils.subtract(inputSpace.edgesOf(c), blendSpace.edgeSet());
			if (edges.isEmpty()) {
				return false;
			}
			// see if the edge connects to a concept involved in the mapping
			for (StringEdge edge : VariousUtils.asShuffledArray(edges, random)) {
				String a = edge.getOppositeOf(c);
				if (mapping.containsConcept(a)) { // other concept is involved in the mapping
					// get the involved concept pair
					ConceptPair<String> conceptPair = mapping.getConceptPair(a);

					// recreate the edge renaming the concept 'a' to either 'b' OR 'a|'b
					String targetRename = null;
					switch (random.nextInt(2)) {
					case 0:
						targetRename = conceptPair.toString();
						break;
					case 1:
						targetRename = conceptPair.getOpposingConcept(a);
						break;
					case 2:
						targetRename = a;
						break;
					}

					StringEdge edgeToAdd = edge.replaceSourceOrTarget(a, targetRename);
					if (blendSpace.addEdge(edgeToAdd)) {
						return true;
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
		// in general: either add a random edge using the mapping or not

		// BOOT blend space (it is empty)
		if (blendSpace.isEmpty()) {
			if (random.nextBoolean()) { // use the mapping
				for (int i = 0; i < 16; i++) {
					// get a random pair from the mapping
					ConceptPair<String> conceptPair = mapping.getRandomPair(random);
					boolean added = addFirstMappingEdge(blendSpace, conceptPair);
					if (added) {
						break;
					}
				}
			} else { // OR do not use the mapping
				addNeighbourEdge(blendSpace, null);
			}
		} else {
			// blend space not empty
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
	/**
	 * adds a random edge using a randomly chosen concept pair (does not connect to existing concept pairs)
	 * 
	 * @param blendSpace
	 * @param conceptPair
	 * @return
	 */
	private static boolean addFirstMappingEdge(StringGraph blendSpace, ConceptPair<String> conceptPair) {
		// first get all edges touching the left and right concepts in the pair
		String l = conceptPair.getLeftConcept();
		String r = conceptPair.getRightConcept();

		Set<StringEdge> newEdges = VariousUtils.mergeSets(inputSpace.edgesOf(l), inputSpace.edgesOf(r));
		if (newEdges.isEmpty()) {
			return false;
		}
		ArrayList<StringEdge> shuffledEdges = VariousUtils.asShuffledArray(newEdges, random);

		// either swap l/r concepts in the new edge OR blend them
		if (random.nextBoolean()) {
			// pick an edge
			for (StringEdge edge : shuffledEdges) {
				String originalConcept, swappedConcept;
				// which concept l/r does the edge have?
				if (edge.containsConcept(l)) { // l becomes r
					originalConcept = l;
					swappedConcept = r;
				} else { // r becomes l
					originalConcept = r;
					swappedConcept = l;
				}
				StringEdge edgeToAdd = edge.replaceSourceOrTarget(originalConcept, swappedConcept);
				if (blendSpace.addEdge(edgeToAdd)) {
					return true;
				}
			}
		} else { // use both concepts from the pair
			String blend = l + "|" + r;
			for (StringEdge edge : shuffledEdges) {
				// add one of the touching edges, replacing left/right concepts with the blended pair
				StringEdge edgeToAdd = edge.replaceSourceOrTarget(l, blend).replaceSourceOrTarget(r, blend);
				if (blendSpace.addEdge(edgeToAdd)) {
					return true;
				}
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
		// GraphAlgorithms.removeSmallerComponents(blendSpace);
	}
}
