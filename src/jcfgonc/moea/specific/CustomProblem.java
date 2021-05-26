package jcfgonc.moea.specific;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import frames.SemanticFrame;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import jcfgonc.blender.logic.LogicUtils;
import jcfgonc.blender.structures.Blend;
import jcfgonc.moea.generic.ProblemDescription;
import structures.Mapping;
import structures.Ticker;
import structures.UnorderedPair;
import utils.JatalogInterface;
import utils.OSTools;
import wordembedding.WordEmbeddingUtils;
import za.co.wstoop.jatalog.DatalogException;
import za.co.wstoop.jatalog.Expr;

public class CustomProblem implements Problem, ProblemDescription {

	private final StringGraph inputSpace;
	private final ArrayList<Mapping<String>> mappings;
	private final RandomAdaptor random;
	private final ArrayList<SemanticFrame> frames;
	private final List<List<Expr>> frameQueries;
	private final Object2DoubleOpenHashMap<UnorderedPair<String>> wps;
	private final Object2DoubleOpenHashMap<String> vitalRelations;
	private ConcurrentLinkedDeque<JatalogInterface> datalogEngines;

	/**
	 * Invoked by Custom Launcher when creating this problem. Custom code typically required here.
	 * 
	 */
	public CustomProblem(StringGraph inputSpace, ArrayList<Mapping<String>> mappings, ArrayList<SemanticFrame> frames,
			Object2DoubleOpenHashMap<String> vitalRelations, Object2DoubleOpenHashMap<UnorderedPair<String>> wps, RandomAdaptor random) {
		this.inputSpace = inputSpace;
		this.mappings = mappings;
		this.frames = frames;
		this.random = random;
		this.vitalRelations = vitalRelations;
		this.wps = wps;

		Ticker ticker = new Ticker();
		// create datalog frame queries
		ticker.resetTicker();
		this.frameQueries = JatalogInterface.createQueriesFromFrames(frames);
		System.out.println("took " + ticker.getElapsedTime() + "s to convert frames to datalog queries");

		datalogEngines = new ConcurrentLinkedDeque<>();
		for (int i = 0; i < OSTools.getNumberOfCores() * 2; i++) {
			datalogEngines.add(new JatalogInterface());
		}
	}

	@Override
	/**
	 * Invoked by MOEA when initializing/filling the solution population. Custom code required here.
	 */
	public Solution newSolution() {
		// create a new blend with empty space and a random mapping
		StringGraph blendSpace = new StringGraph();
		Mapping<String> mapping = mappings.get(random.nextInt(mappings.size()));
		Blend b = new Blend(blendSpace, mapping, 0);

		CustomChromosome cc = new CustomChromosome(b);

		// do not touch this unless the solution domain X has more than one dimension
		Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives(), getNumberOfConstraints());
		solution.setVariable(0, cc);
		return solution;
	}

	@Override
	/**
	 * Invoked by MOEA when evaluating a solution. Custom code required here. Remember to define the variable's descriptions in
	 * MOConfig.OBJECTIVE_DESCRIPTION
	 */
	public void evaluate(Solution solution) {
		CustomChromosome pc = (CustomChromosome) solution.getVariable(0); // unless the solution domain X has more than one dimension
		Blend blend = pc.getBlend();
		StringGraph blendSpace = blend.getBlendSpace();
		Mapping<String> mapping = blend.getMapping();

		// get a datalog engine
		JatalogInterface datalog = datalogEngines.pop();
		BitSet queriesResults = null;
		// check for frames matched in the blend
		try {
			// transform the blend space into a KB
			datalog.clearFacts(); // very important, clear previous stuff
			datalog.addFacts(blendSpace);
			// do the queries
			queriesResults = datalog.isQueryTrue(frameQueries);
		} catch (DatalogException e) {
			e.printStackTrace();
		} finally {
			datalogEngines.push(datalog);
		}

		// calculate frame qualities
		// process queries/frames results
		int cardinality = queriesResults.cardinality();
		// 10 is an acceptable limit to fit within a small graphical window
		// (i.e. if it was +Integer.MAX it would be hard to see small values)
		int edgesLargestFrame = 0; // maximize
		int numberMatchedFrames = cardinality;// 10; // minimize
		if (cardinality > 0) {
//			if (cardinality < numberMatchedFrames) {
//				numberMatchedFrames = cardinality;
//			}
			// iterate through the set bits in the bitset
			for (int i = queriesResults.nextSetBit(0); i != -1; i = queriesResults.nextSetBit(i + 1)) {
				SemanticFrame frame = frames.get(i);
				// find largest frame
				StringGraph frameGraph = frame.getFrame();
				int numberOfEdges = frameGraph.numberOfEdges();
				if (numberOfEdges > edgesLargestFrame) {
					edgesLargestFrame = numberOfEdges;
				}
			}
		}

		// now experimenting with blend SS instead of frames SS
//		double frameSemanticSimilarity;
//		if (matchedFrames.isEmpty()) {
//			frameSemanticSimilarity = Double.MAX_VALUE;
//		} else {
//			frameSemanticSimilarity = 0;
//			for (Entry<SemanticFrame> matchedFrame : matchedFrames.object2IntEntrySet()) {
//				SemanticFrame frame = matchedFrame.getKey();
//				// TODO perhaps mean of the means?
//				frameSemanticSimilarity += frame.getSemanticSimilarityMean();
//			}
//		}

		// blend edge semantic similarity
		double blendSemanticSimilarity = 0.0;
		if (blendSpace.numberOfEdges() >= 2) { // measuring similarity requires at least two edges
			// this array contains pairwise similarities of nearby edges
			double[] edgePairsSimilarities = WordEmbeddingUtils.calculateEdgeSemanticSimilarity(blendSpace, wps);
			DescriptiveStatistics ds = new DescriptiveStatistics(edgePairsSimilarities);
			blendSemanticSimilarity = ds.getMean(); // ds.getMean();
			if (blendSemanticSimilarity > 1.1) {
				System.err.println("questionable semantic similarity for graph " + blendSpace + " = " + Arrays.toString(edgePairsSimilarities));
			}
		}

		// mapping usage
		double mappingMix = (double) LogicUtils.calculateMappingMix(blendSpace, mapping) / blendSpace.numberOfEdges();

		// balance between the two input spaces
		double inputSpacesBalance = LogicUtils.calculateInputSpacesBalance(blendSpace, mapping);

		// cycles
//		int cycles = GraphAlgorithms.countCycles(blendSpace);

		// vital relations
		double vitalRelationsScore = LogicUtils.evaluateVitalRelations(blendSpace, vitalRelations);
		// maximize the least important relation (and consequently the remaining relations which are more important)
		// double vitalRelationsScore = LogicUtils.calculateMeanImportantanceVitalRelations(blendSpace, vitalRelations);

		// percentage of blended concepts in the blend space (0...1)
		double blendedConcepts = LogicUtils.calculateBlendedConceptsPercentage(blendSpace);

		double meanWordsPerConcept = LogicUtils.calculateWordsPerConceptScore(blendSpace);

		// relation statistics
//		double[] rs = LogicUtils.calculateRelationStatistics(blendSpace);
//		double relationStdDev = rs[1]; // 0...1

		// set solution's objectives here
		int obj_i = 0;
		solution.setObjective(obj_i++, blendSemanticSimilarity);
		solution.setObjective(obj_i++, -vitalRelationsScore);

		solution.setObjective(obj_i++, -inputSpacesBalance);
		solution.setObjective(obj_i++, -mappingMix);
		solution.setObjective(obj_i++, blendedConcepts);
		solution.setObjective(obj_i++, meanWordsPerConcept);

		// solution.setObjective(obj_i++, -cycles);
		solution.setObjective(obj_i++, -edgesLargestFrame);// 7 is the expected max edges of largest frame
		solution.setObjective(obj_i++, numberMatchedFrames);// 20 is the expected max of number of matched frames and 1 the lowest

//		solution.setObjective(obj_i++, relationStdDev - 1);
//		solution.setObjective(obj_i++, -numEdges);

//		System.out.printf("%d\t%d\t%d\n", mappingMix, blendSpace.numberOfEdges(), blendSpace.numberOfVertices());

		// if required define constraints below
		// violated constraints are set to 1, otherwise set to 0
		if (blendSpace.numberOfVertices() < 3 || blendSpace.numberOfVertices() > 6) { // range number of vertices in the blend space
			solution.setConstraint(0, 1);
		} else {
			solution.setConstraint(0, 0);
		}

		if (inputSpacesBalance < 0.1) { // input space's ratio at least 0.1
			solution.setConstraint(1, 1);
		} else {
			solution.setConstraint(1, 0);
		}

	}

	private String[] objectivesDescription = { //
			"f:mean within-blend semantic similarity", //
			"f:mean importance of vital relations", //
			"f:input spaces balance", //
			"f:mapping mix", //
			"f:blended concepts", //
			"f:mean of words per concept", //
			
			// "d:number of cycles", //
			
			"d:number of edges of largest frame", //
			"d:number of matched frames", //

//			"f:relation balance", //
//			"d:number of edges", //
	};

	private String[] constraintsDescription = { //
			"required number of vertices", //
			"input spaces ratio" };

	@Override
	/**
	 * The number of objectives defined by this problem.
	 */
	public int getNumberOfObjectives() {
		return objectivesDescription.length;
	}

	@Override
	public String getObjectiveDescription(int varid) {
		return objectivesDescription[varid];
	}

	@Override
	/**
	 * The number of constraints defined by this problem.
	 */
	public int getNumberOfConstraints() {
		return constraintsDescription.length;
	}

	@Override
	public String getConstraintsDescription(int varid) {
		return constraintsDescription[varid];
	}

	@Override
	/**
	 * NOT IMPLEMENTED: this is supposed to be used somewhere, I don't know where (probably in the GUI's title?)
	 */
	public String getProblemDescription() {
		return "Conceptual Blender: Multi-Objective version";
	}

	@Override
	public String getVariableDescription(int varid) {
		return "g:blend space";
	}

	public StringGraph getInputSpace() {
		return inputSpace;
	}

	public List<Mapping<String>> getMappings() {
		return mappings;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	/**
	 * The number of variables defined by this problem.
	 */
	public int getNumberOfVariables() {
		return 1; // blend space/graph
	}

	@Override
	public void close() {
	}

}
