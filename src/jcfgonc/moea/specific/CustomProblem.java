package jcfgonc.moea.specific;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.Query;

import frames.SemanticFrame;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import jcfgonc.blender.BlenderMoConfig;
import jcfgonc.blender.logic.LogicUtils;
import jcfgonc.blender.structures.Blend;
import jcfgonc.moea.generic.ProblemDescription;
import structures.Mapping;
import structures.UnorderedPair;
import wordembedding.WordEmbeddingUtils;

public class CustomProblem implements Problem, ProblemDescription {

	private final StringGraph inputSpace;
	private final ArrayList<Mapping<String>> mappings;
	private final RandomAdaptor random;
	private final ArrayList<SemanticFrame> frames;
	private final List<Query> frameQueries;
	private final Object2DoubleOpenHashMap<UnorderedPair<String>> wps;
	private final Object2DoubleOpenHashMap<String> vitalRelations;

	/**
	 * Invoked by Custom Launcher when creating this problem. Custom code typically required here.
	 * 
	 */
	public CustomProblem(StringGraph inputSpace, ArrayList<Mapping<String>> mappings, ArrayList<SemanticFrame> frames, ArrayList<Query> frameQueries,
			Object2DoubleOpenHashMap<String> vitalRelations, Object2DoubleOpenHashMap<UnorderedPair<String>> wps, RandomAdaptor random) {
		this.inputSpace = inputSpace;
		this.mappings = mappings;
		this.frames = frames;
		this.random = random;
		this.frameQueries = frameQueries;
		this.vitalRelations = vitalRelations;
		this.wps = wps;
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

		// transform the blend space into a KB
		KnowledgeBase blendKB = LogicUtils.buildKnowledgeBase(blendSpace);
		// check for frames matched in the blend
		Object2IntOpenHashMap<SemanticFrame> matchedFrames = new Object2IntOpenHashMap<SemanticFrame>();
		int edgesLargestFrame = 0;
		for (int i = 0; i < frames.size(); i++) {
			SemanticFrame frame = frames.get(i);
			Query frameQuery = frameQueries.get(i);
			int count = LogicUtils.countFrameMatchesInt(blendKB, frameQuery, BlenderMoConfig.BLOCK_SIZE, BlenderMoConfig.PARALLEL_LIMIT,
					BlenderMoConfig.SOLUTION_LIMIT, true, Long.valueOf(BlenderMoConfig.QUERY_TIMEOUT_SECONDS));
			if (count > 0) {
				matchedFrames.put(frame, count);
				// find largest frame
				int numberOfEdges = frame.getGraph().numberOfEdges();
				if (numberOfEdges > edgesLargestFrame) {
					edgesLargestFrame = numberOfEdges;
				}
			}
		}
		int numberMatchedFrames = matchedFrames.size();
		// the MOEA will minimize numberMatchedFrames and because we want blends to have at least one frame and not many
		// we'll penalize zero frames
		final int frameLimit = 20;
		if (numberMatchedFrames == 0 || numberMatchedFrames > frameLimit) {
			numberMatchedFrames = frameLimit;
		}
		matchedFrames = null;
		blendKB = null;

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
		double blendSemanticSimilarity = 1; // set to 1 (the max) by default because we want to minimize this
		if (blendSpace.numberOfEdges() >= 2) { // measuring similarity requires at least two edges
			double[] sim = WordEmbeddingUtils.calculateEdgeSemanticSimilarity(blendSpace, wps);
			DescriptiveStatistics ds = new DescriptiveStatistics(sim);
			blendSemanticSimilarity = ds.getMean();
		}

		// count mappings
		int numberMappings = LogicUtils.countMappings(blendSpace);

		// cycles
//		int cycles = GraphAlgorithms.countCycles(blendSpace);

		// vital relations
		double vrScore = LogicUtils.evaluateVitalRelations(blendSpace, vitalRelations);

		// relation statistics
		double[] rs = LogicUtils.calculateRelationStatistics(blendSpace);
		double relationStdDev = rs[1]; // 0...1

		double is_balance = LogicUtils.calculateInputSpacesBalance(blendSpace, mapping);

		// set solution's objectives here
		int obj_i = 0;
		solution.setObjective(obj_i++, relationStdDev);
		solution.setObjective(obj_i++, -numberMappings);// 20 is the expected max of number of mappings
		solution.setObjective(obj_i++, -edgesLargestFrame);// 7 is the expected max edges of largest frame
		// solution.setObjective(obj_i++, -cycles);
		solution.setObjective(obj_i++, numberMatchedFrames);// 20 is the expected max of number of matched frames and 1 the lowest
		solution.setObjective(obj_i++, blendSemanticSimilarity);
		solution.setObjective(obj_i++, -vrScore);
		solution.setObjective(obj_i++, -is_balance);

		if (blendSpace.numberOfVertices() == 0) {
			System.err.println("blendSpace.numberOfVertices() == 0");
		}

		if (numberMappings > blendSpace.numberOfVertices()) {
			System.err.println("numberMappings > blendSpace.numberOfVertices()");
		}

//		if (frameSemanticSimilarity < 1000 && matchedFrames.size() == 0) {
//			System.err.println("semanticSimilarityMaxSum < 1000 && matchedFrames.size() == 0");
//		}

		// if required define constraints below
		// violated constraints are set to 1, otherwise set to 0

		// range number of vertices in the blend space
		if (blendSpace.numberOfVertices() > 10) {
			solution.setConstraint(0, 1);
		} else {
			solution.setConstraint(0, 0);
		}
		// maximum number of matched frames
		// TODO remover limite de 4, isto e capaz de ser grave
		if (numberMatchedFrames > 100) {
			solution.setConstraint(1, 0);
		} else {
			solution.setConstraint(1, 0);
		}
		// require one or more frames
		if (numberMatchedFrames < 0) {
			solution.setConstraint(2, 1);
		} else {
			solution.setConstraint(2, 0);
		}
		// require semanticSimilarityMaxSum below threshold
		if (blendSemanticSimilarity > 1) {
			solution.setConstraint(3, 1);
		} else {
			solution.setConstraint(3, 0);
		}

	}

	@Override
	/**
	 * The number of objectives defined by this problem.
	 */
	public int getNumberOfObjectives() {
		return 7;
	}

	@Override
	public String getObjectiveDescription(int varid) {
		String[] objectives = { //
				"f:relation stddev", //
				"d:number of concept pairs", //
				"d:number of edges of largest frame", //
				// "d:number of cycles", //
				"d:number of matched frames", //
				"f:mean of within-blend semantic similarity", //
				"f:mean importance of vital relations", //
				"f:input spaces balance" };
		return objectives[varid];
	}

	@Override
	/**
	 * The number of constraints defined by this problem.
	 */
	public int getNumberOfConstraints() {
		return 4;
	}

	@Override
	public String getConstraintDescription(int varid) {
		return "TODO";
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
