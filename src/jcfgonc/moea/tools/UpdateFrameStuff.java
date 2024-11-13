package jcfgonc.moea.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

	public static Object2DoubleOpenHashMap<String> readVitalRelations(String path) throws IOException {
		Object2DoubleOpenHashMap<String> relationToImportance = new Object2DoubleOpenHashMap<String>();
		BufferedReader br = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8), 1 << 24);
		String line;
		boolean firstLine = true;
		while ((line = br.readLine()) != null) {
			if (firstLine) {
				firstLine = false;
				continue;
			}
			String[] cells = VariousUtils.fastSplitWhiteSpace(line);
			String relation = cells[0];
			double importance = Double.parseDouble(cells[1]);
			relationToImportance.put(relation, importance);
		}
		br.close();
		System.out.printf("using the definition of %d vital relations from %s\n", relationToImportance.size(), path);
		return relationToImportance;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {

		// read frames file
		String framespath = "D:\\Desktop\\Java - PhD\\PatternMiner\\results\\pattern_resultsV2.2.tsv";
		ArrayList<SemanticFrame> frames = FrameReadWrite.readPatternFrames(framespath);

		// read vital relations importance
		Object2DoubleOpenHashMap<String> vitalRelations = readVitalRelations(MOEA_Config.vitalRelationsPath);

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
