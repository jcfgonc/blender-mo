package jcfgonc.moea.tools;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import frames.FrameReadWrite;
import frames.SemanticFrame;
import graph.GraphAlgorithms;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import jcfgonc.blender.MOEA_Config;
import jcfgonc.blender.logic.LogicUtils;
import utils.VariousUtils;

public class UpdateFrameStuff {

	public static void main(String[] args) throws IOException, InterruptedException {

		// read frames file
		String framespath = "D:\\Desktop\\Java - PhD\\PatternMiner\\results\\pattern_resultsV2.2.tsv";
		ArrayList<SemanticFrame> frames = FrameReadWrite.readPatternFrames(framespath);

		// read vital relations importance
		Object2DoubleOpenHashMap<String> vitalRelations = VariousUtils
				.readVitalRelations(MOEA_Config.vitalRelationsPath);

		ArrayList<SemanticFrame> framesNew = new ArrayList<SemanticFrame>(frames.size());

		// update stuff
		for (SemanticFrame frame : frames) {
			StringGraph frameGraph = frame.getFrame();
			//HashSet<String> labelSet = frameGraph.getEdgeLabelSet();

			// if (labelSet.contains("antonym"))
			// continue;

			DescriptiveStatistics vrDS = LogicUtils.calculatePresenceVitalRelations(frameGraph, vitalRelations);
			frame.setVitalRelationsMean(vrDS.getMean());
			frame.setVitalRelationsMin(vrDS.getMin());

			String hdv = GraphAlgorithms.getHighestDegreeVertex(frameGraph);
			int degreeHdv = frameGraph.degreeOf(hdv);
			frame.setHighestVertexDegree(degreeHdv);

			framesNew.add(frame);
		}
		FrameReadWrite.writePatternFramesCSV(framesNew,
				VariousUtils.appendSuffixToFilename(framespath, "_updated"));
	}

}
