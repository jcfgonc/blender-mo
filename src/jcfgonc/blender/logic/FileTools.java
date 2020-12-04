package jcfgonc.blender.logic;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

import com.githhub.aaronbembenek.querykb.Query;

import graph.GraphReadWrite;
import graph.StringGraph;
import structures.Mapping;
import structures.Ticker;

public class FileTools {
	public static List<Query> readPatternFramesCSV_OLD(String framesPath) throws IOException {
		System.out.println("loading frames from " + framesPath);
		Ticker ticker = new Ticker();
		List<StringGraph> framesOriginal = LogicUtils.readPatternResultsDelimitedFile(new File(framesPath), " \t", true, 7);

		// create querykb queries from the frames
		List<Query> frames = new ArrayList<Query>();
		for (StringGraph frame : framesOriginal) {
			if (frame.numberOfEdges() > 2 && frame.numberOfEdges() < 100) {
				frames.add(LogicUtils.createQueryFromStringGraph(frame));
			}
		}
		System.out.printf("using %d from %d frames\n", frames.size(), framesOriginal.size());
		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");
		framesOriginal = null;
		System.out.println("-------");
		return frames;
	}

	public static StringGraph readInputSpace(String inputSpacePath) throws IOException, NoSuchFileException {
		System.out.println("loading input space from " + inputSpacePath);
		StringGraph inputSpace = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		Ticker ticker = new Ticker();
		GraphReadWrite.readCSV(inputSpacePath, inputSpace);
		inputSpace.showStructureSizes();
		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");
		System.out.println("-------");
		return inputSpace;
	}

	public static ArrayList<Mapping<String>> readMappings(String mappingPath) throws IOException {
		System.out.println("loading mappings from " + mappingPath);
		Ticker ticker = new Ticker();
		ArrayList<Mapping<String>> mappingsOriginal = Mapping.readMultipleMappingsCSV(new File(mappingPath));
		ArrayList<Mapping<String>> mappings = new ArrayList<Mapping<String>>();
		for (Mapping<String> mapping : mappingsOriginal) {
		//	if (mapping.getSize() > 4 && mapping.size() < 100) {
				mappings.add(mapping);
		//	}
		}
		System.out.printf("using %d from %d mappings\n", mappings.size(), mappingsOriginal.size());
		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");
		System.out.println("-------");
		return mappings;
	}

}
