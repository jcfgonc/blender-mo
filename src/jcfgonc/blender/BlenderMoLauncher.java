package jcfgonc.blender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.SynchronizedRandomGenerator;
import org.apache.commons.math3.random.Well44497b;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.core.spi.OperatorProvider;
import org.moeaframework.util.TypedProperties;

import frames.FrameReadWrite;
import frames.SemanticFrame;
import graph.GraphReadWrite;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import jcfgonc.moea.generic.InteractiveExecutor;
import jcfgonc.moea.specific.CustomMutation;
import jcfgonc.moea.specific.CustomProblem;
import jcfgonc.moea.specific.ResultsWriter;
import jcfgonc.moea.specific.ResultsWriterBlenderMO;
import structures.Mapping;
import structures.Ticker;
import structures.UnorderedPair;
import utils.VariousUtils;
import wordembedding.WordEmbeddingUtils;

public class BlenderMoLauncher {

	private static void registerCustomMutation() {
		OperatorFactory.getInstance().addProvider(new OperatorProvider() {
			public String getMutationHint(Problem problem) {
				return null;
			}

			public String getVariationHint(Problem problem) {
				return null;
			}

			public Variation getVariation(String name, Properties properties, Problem problem) {
				TypedProperties typedProperties = new TypedProperties(properties);

				if (name.equalsIgnoreCase("CustomMutation")) {
					double probability = typedProperties.getDouble("CustomMutation.Rate", 1.0);
					CustomMutation pm = new CustomMutation(probability);
					return pm;
				}

				// No match, return null
				return null;
			}
		});
	}

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

	public static void main(String[] args) throws NoSuchFileException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException, InterruptedException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		RandomAdaptor random = new RandomAdaptor(new SynchronizedRandomGenerator(new Well44497b()));

		// read input space
		StringGraph inputSpace = readInputSpace(MOEA_Config.inputSpacePath);

		// read mappings file (contains multiple mappings)
		ArrayList<Mapping<String>> mappings = Mapping.readMappingsCSV(new File(MOEA_Config.mappingPath));
		System.out.printf("using %d mappings\n", mappings.size());

		// read vital relations importance
		Object2DoubleOpenHashMap<String> vitalRelations = readVitalRelations(MOEA_Config.vitalRelationsPath);

		// read frames file
		ArrayList<SemanticFrame> frames = FrameReadWrite.readPatternFrames(MOEA_Config.framesPath);

		System.out.printf("using %d frames\n", frames.size());

		// read pre-calculated semantic scores of word/relation pairs
		Object2DoubleOpenHashMap<UnorderedPair<String>> wps = WordEmbeddingUtils.readWordPairScores(MOEA_Config.wordPairScores_filename);

		// setup the mutation and the MOEA
		registerCustomMutation();
		Properties properties = new Properties();
		properties.setProperty("operator", "CustomMutation");
		properties.setProperty("CustomMutation.Rate", "1.0");

		// eNSGA-II
		properties.setProperty("epsilon", "0.0001"); // default is 0.01
		properties.setProperty("windowSize", "256"); // epoch to trigger eNSGA2 population injection
		properties.setProperty("maxWindowSize", "480"); // epoch to trigger eNSGA2 hard restart
//		properties.setProperty("injectionRate", Double.toString(1.0 / 0.25)); // population to archive ratio, default is 0.25

		// NSGA-III
//		properties.setProperty("divisionsOuter", "10"); // 3
//		properties.setProperty("divisionsInner", "1"); // 2

		BlendMutation.setInputSpace(inputSpace);
		BlendMutation.setRandom(random);

		String dateTimeStamp = VariousUtils.generateCurrentDateAndTimeStamp();
		String resultsFilename = String.format("moea_results_%s.tsv", dateTimeStamp);
		// personalize your results writer here
		ResultsWriter resultsWriter = new ResultsWriterBlenderMO();

		// personalize your constructor here
		CustomProblem problem = new CustomProblem(inputSpace, mappings, frames, vitalRelations, wps, random);

		InteractiveExecutor ie = new InteractiveExecutor(problem, properties, resultsFilename, resultsWriter);

		resultsWriter.writeFileHeader(resultsFilename, problem);

		// do 'k' runs of 'n' epochs
		int totalRuns = MOEA_Config.MOEA_RUNS;
		// ArrayList<NondominatedPopulation> allResults = new ArrayList<NondominatedPopulation>(totalRuns);
		for (int moea_run = 0; moea_run < totalRuns; moea_run++) {
			if (ie.isCanceled())
				break;
			// properties.setProperty("maximumPopulationSize", Integer.toString(MOEA_Config.POPULATION_SIZE * 2)); // default is 10 000
			properties.setProperty("populationSize", Integer.toString(MOEA_Config.POPULATION_SIZE));
			// do one run of 'n' epochs
			NondominatedPopulation currentResults = ie.execute(moea_run);
			// allResults.add(currentResults);
			resultsWriter.appendResultsToFile(resultsFilename, currentResults, problem);
		}
		resultsWriter.close();
		ie.closeGUI();
		// mergeAndSaveResults(String.format("moea_results_%s_merged.tsv", dateTimeStamp), allResults, problem, 0.01);

		// terminate daemon threads
		System.exit(0);
	}

	@SuppressWarnings("unused")
	private static ArrayList<Mapping<String>> filterMappings(ArrayList<Mapping<String>> mappings) {
		ArrayList<Mapping<String>> mappingsNew = new ArrayList<Mapping<String>>(mappings.size());
		for (Mapping<String> mapping : mappings) {
			if (mapping.getSize() < 100) {
				mappingsNew.add(mapping);
			}
		}
		return mappingsNew;
	}

	@SuppressWarnings("unused")
	private static ArrayList<SemanticFrame> filterFrames(ArrayList<SemanticFrame> frames) {
		// filter (or not) some frames
		ArrayList<SemanticFrame> framesNew = new ArrayList<SemanticFrame>(frames.size());
		for (SemanticFrame frame : frames) {
//			if (frame.getSemanticSimilarityMean() > 0.0)
//				continue;
//			if (frame.getFrame().numberOfEdges() > 10)
//				continue;
//			if (frame.getMatches() < 2) // less than 100 (10^2) occurrences
//				continue;
//			if (frame.getRelationTypesStd() > 0.01)
//				continue;
//			if (frame.getCycles() > 10)
//				continue;
//			if (frame.getEdgesPerRelationTypes() > 1.1)
//				continue;
			framesNew.add(frame);
		}
		return framesNew;
	}

	/**
	 * Merge the separate results into a single population using &epsilon;-box dominance and save them to a file.
	 * 
	 * @param filename
	 * @param allResults
	 * @param problem
	 */
	@SuppressWarnings("unused")
	private static void mergeAndSaveResults(String filename, ArrayList<NondominatedPopulation> allResults, CustomProblem problem, double epsilon) {
		NondominatedPopulation mergedResults = new NondominatedPopulation();
		// new EpsilonBoxDominanceArchive(epsilon);
		for (NondominatedPopulation result : allResults) {
			for (Solution solution : result) {
				mergedResults.add(solution);
			}
		}
		ResultsWriter resultsWriter = new ResultsWriterBlenderMO();
		resultsWriter.writeFileHeader(filename, problem);
		resultsWriter.appendResultsToFile(filename, mergedResults, problem);
		resultsWriter.close();
	}

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
}
