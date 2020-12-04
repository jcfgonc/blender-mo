package jcfgonc.moea.specific;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomAdaptor;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.Query;

import frames.SemanticFrame;
import graph.GraphAlgorithms;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import jcfgonc.blender.BlenderMoConfig;
import jcfgonc.blender.logic.LogicUtils;
import jcfgonc.blender.structures.Blend;
import jcfgonc.moea.generic.ProblemDescription;
import structures.Mapping;
import structures.UnorderedPair;

public class CustomProblem implements Problem, ProblemDescription {

	private StringGraph inputSpace;
	private ArrayList<Mapping<String>> mappings;
	private RandomAdaptor random;
	private ArrayList<SemanticFrame> frames;
	private List<Query> frameQueries;
	private Object2DoubleOpenHashMap<UnorderedPair<String>> wps;

	/**
	 * Invoked by Custom Launcher when creating this problem. Custom code typically required here.
	 * 
	 * @param inputSpace
	 * @param mappings
	 * @param frames
	 * @param frameQueries
	 * @param random
	 */
	public CustomProblem(StringGraph inputSpace, ArrayList<Mapping<String>> mappings, ArrayList<SemanticFrame> frames, ArrayList<Query> frameQueries,
			RandomAdaptor random) {
		this.setInputSpace(inputSpace);
		this.setMappings(mappings);
		this.frames = frames;
		this.random = random;
		this.frameQueries = frameQueries;
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

		// transform the blend space into a KB
		KnowledgeBase blendKB = LogicUtils.buildKnowledgeBase(blendSpace);
		// check for frames matched in the blend
		Object2IntOpenHashMap<SemanticFrame> matchedFrames = new Object2IntOpenHashMap<SemanticFrame>();
		for (int i = 0; i < frames.size(); i++) {
			SemanticFrame frame = frames.get(i);
			Query frameQuery = frameQueries.get(i);
			int count = LogicUtils.countFrameMatchesInt(blendKB, frameQuery, BlenderMoConfig.BLOCK_SIZE, BlenderMoConfig.PARALLEL_LIMIT,
					BlenderMoConfig.SOLUTION_LIMIT, true, Long.valueOf(BlenderMoConfig.QUERY_TIMEOUT_SECONDS));
			if (count > 0) {
				matchedFrames.put(frame, count);
			}
		}
		// Object2IntOpenHashMap<String> relationCount = GraphAlgorithms.countRelations(blendSpace);

		// set frame objectives
		int largestFrameEdgeCount = 0;
		double frameSemanticSimilarity;
		if (matchedFrames.isEmpty()) {
			frameSemanticSimilarity = Double.MAX_VALUE;
		} else {
			frameSemanticSimilarity = 0;
			for (Entry<SemanticFrame> matchedFrame : matchedFrames.object2IntEntrySet()) {
				SemanticFrame frame = matchedFrame.getKey();
				// int count = matchedFrame.getIntValue();

//				if (relationCount.getInt("influencedby") > 4) {
//					if (frame.getGraph().numberOfEdges() > 5) {
//						System.lineSeparator();
//					}
//				}

				int numberOfEdges = frame.getGraph().numberOfEdges();
				if (numberOfEdges > largestFrameEdgeCount) {
					largestFrameEdgeCount = numberOfEdges;
				}
				// TODO perhaps mean of the means?
				frameSemanticSimilarity += frame.getSemanticSimilarityMean();
			}
		}

		// count mappings
		int numberMappings = LogicUtils.countMappings(blendSpace);

		// cycles
		int cycles = GraphAlgorithms.countCycles(blendSpace);

		// set solution's objectives here
		solution.setObjective(0, -blendSpace.numberOfVertices());// -filterFun(10, blendSpace.numberOfVertices()));
		solution.setObjective(1, -numberMappings);
		solution.setObjective(2, -largestFrameEdgeCount);
		solution.setObjective(3, -cycles);
		solution.setObjective(4, frameSemanticSimilarity);
		solution.setObjective(5, -matchedFrames.size());

		if (blendSpace.numberOfVertices() == 0) {
			System.err.println("blendSpace.numberOfVertices() == 0");
		}

		if (numberMappings > blendSpace.numberOfVertices()) {
			System.err.println("numberMappings > blendSpace.numberOfVertices()");
		}

		if (frameSemanticSimilarity < 1000 && matchedFrames.size() == 0) {
			System.err.println("semanticSimilarityMaxSum < 1000 && matchedFrames.size() == 0");
		}

		// if required define constraints below
		// violated constraints are set to 1, otherwise set to 0

		// number of vertices in the blend space
		if (blendSpace.numberOfVertices() > 10) {
			solution.setConstraint(0, 1);
		} else {
			solution.setConstraint(0, 0);
		}
		// maximum number of matched frames
		if (matchedFrames.size() > 4) {
			solution.setConstraint(1, 1);
		} else {
			solution.setConstraint(1, 0);
		}
		// require one or more frames
		if (matchedFrames.size() == 0) {
			solution.setConstraint(2, 1);
		} else {
			solution.setConstraint(2, 0);
		}
		// require semanticSimilarityMaxSum below threshold
		if (frameSemanticSimilarity > 3) {
			solution.setConstraint(3, 1);
		} else {
			solution.setConstraint(3, 0);
		}

	}

	@SuppressWarnings("unused")
	private int filterFun(int x, int k) {
		if (x > k)
			return 0;
		else
			return x;
	}

	@Override
	/**
	 * The number of objectives defined by this problem.
	 */
	public int getNumberOfObjectives() {
		return 6;
	}

	@Override
	public String getObjectiveDescription(int varid) {
		String[] objectives = { "#blend vertices", "#concept pairs", "#edges largest frame", "#cycles", "frame SS", "#matched frames" };
		return objectives[varid];
//		return "TODO";
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
		return "Blend Space";
	}

	public StringGraph getInputSpace() {
		return inputSpace;
	}

	public void setInputSpace(StringGraph inputSpace) {
		this.inputSpace = inputSpace;
	}

	public List<Mapping<String>> getMappings() {
		return mappings;
	}

	public void setMappings(ArrayList<Mapping<String>> mappings) {
		this.mappings = mappings;
	}

	public void setWordPairsSemanticSimilarity(Object2DoubleOpenHashMap<UnorderedPair<String>> wps) {
		this.wps = wps;
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
