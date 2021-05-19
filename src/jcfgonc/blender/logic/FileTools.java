package jcfgonc.blender.logic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;

import graph.GraphReadWrite;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import structures.Ticker;
import utils.VariousUtils;

public class FileTools {

	public static StringGraph readInputSpace(String inputSpacePath) throws IOException, NoSuchFileException {
		System.out.println("loading input space from " + inputSpacePath);
		StringGraph inputSpace = new StringGraph();
		Ticker ticker = new Ticker();
		GraphReadWrite.readCSV(inputSpacePath, inputSpace);
		inputSpace.showStructureSizes();
		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");
		System.out.println("-------");
		return inputSpace;
	}

	public static Object2DoubleOpenHashMap<String> readVitalRelations(String path) throws IOException {
		Object2DoubleOpenHashMap<String> relationToImportance = new Object2DoubleOpenHashMap<String>();
		BufferedReader br = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8), 1 << 24);
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.contains(":")) // header, eg, s:relation
				continue;
			String[] cells = VariousUtils.fastSplitWhiteSpace(line);
			String relation = cells[0];
			double importance = Double.parseDouble(cells[1]);
			relationToImportance.put(relation, importance);
		}
		br.close();
		System.out.printf("using the definition of %d vital relations from %s\n", relationToImportance.size(), path);
		return relationToImportance;
	}

}
