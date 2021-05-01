package jcfgonc.blender;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.Well44497b;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.core.spi.OperatorProvider;
import org.moeaframework.util.TypedProperties;

import frames.FrameReadWrite;
import frames.SemanticFrame;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import jcfgonc.blender.logic.FileTools;
import jcfgonc.moea.generic.InteractiveExecutor;
import jcfgonc.moea.specific.CustomMutation;
import jcfgonc.moea.specific.CustomProblem;
import jcfgonc.moea.specific.ResultsWriter;
import jcfgonc.moea.specific.ResultsWriterBlenderMO;
import structures.Mapping;
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

	public static void main(String[] args) throws NoSuchFileException, IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		RandomAdaptor random = new RandomAdaptor(new Well44497b());

		// read input space
		StringGraph inputSpace = FileTools.readInputSpace(MOEA_Config.inputSpacePath);

		// read mappings file (contains multiple mappings)
		ArrayList<Mapping<String>> mappings = FileTools.readMappings(MOEA_Config.mappingPath);

		// read frames file
		ArrayList<SemanticFrame> frames0 = FrameReadWrite.readPatternFrames(MOEA_Config.framesPath);
		// filter some frames
		ArrayList<SemanticFrame> frames = new ArrayList<SemanticFrame>(64 * 1024);
		for (SemanticFrame frame : frames0) {
			if (frame.getFrame().numberOfEdges() > 10)
				continue;
			if (frame.getMatches() < 2) // less than 100 (10^2) occurrences
				continue;
			if (frame.getRelationTypesStd() > 0.01)
				continue;
			if (frame.getCycles() > 10)
				continue;
			if (frame.getEdgesPerRelationTypes() > 1.1)
				continue;
			frames.add(frame);
		}
		// frames = new ArrayList<SemanticFrame>(frames.subList(0, 1024));
		System.out.printf("using %d frames\n", frames.size());
		frames0 = null;

		// read pre-calculated semantic scores of word/relation pairs
		Object2DoubleOpenHashMap<UnorderedPair<String>> wps = WordEmbeddingUtils.readWordPairScores(MOEA_Config.wordPairScores_filename);

		// read vital relations importance
		Object2DoubleOpenHashMap<String> vitalRelations = FileTools.readVitalRelations(MOEA_Config.vitalRelationsPath);

		// // test the mutation using a custom GUI
		// TestMutation.testMutation(inputSpace, mappings);

		System.gc();

		// setup the mutation and the MOEA
		registerCustomMutation();
		Properties properties = new Properties();
		properties.setProperty("operator", "CustomMutation");
		properties.setProperty("CustomMutation.Rate", "1.0");
		// eNSGA-II
		properties.setProperty("epsilon", "0.01"); // default is 0.01
		properties.setProperty("windowSize", "99999"); // epoch to trigger eNSGA2 population injection
		properties.setProperty("maxWindowSize", "99999"); // epoch to trigger eNSGA2 hard restart
//		properties.setProperty("injectionRate", Double.toString(1.0 / 0.25)); // population to archive ratio, default is 0.25
		// NSGA-III
		properties.setProperty("divisionsOuter", "10"); // 3
		properties.setProperty("divisionsInner", "0"); // 2

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
		ArrayList<NondominatedPopulation> allResults = new ArrayList<NondominatedPopulation>(totalRuns);
		for (int moea_run = 0; moea_run < totalRuns; moea_run++) {
			if (ie.isCanceled())
				break;
			// properties.setProperty("maximumPopulationSize", Integer.toString(MOEA_Config.POPULATION_SIZE * 2)); // default is 10 000
			properties.setProperty("populationSize", Integer.toString(MOEA_Config.POPULATION_SIZE));
			// do one run of 'n' epochs
			NondominatedPopulation currentResults = ie.execute(moea_run);
			allResults.add(currentResults);

			resultsWriter.appendResultsToFile(resultsFilename, currentResults, problem);
		}
		resultsWriter.close();
		ie.closeGUI();
		mergeAndSaveResults(String.format("moea_results_%s_merged.tsv", dateTimeStamp), allResults, problem, 0.01);

		// terminate daemon threads
		System.exit(0);
	}

	/**
	 * Merge the separate results into a single population using &epsilon;-box dominance and save them to a file.
	 * 
	 * @param filename
	 * @param allResults
	 * @param problem
	 */
	private static void mergeAndSaveResults(String filename, ArrayList<NondominatedPopulation> allResults, CustomProblem problem, double epsilon) {
		EpsilonBoxDominanceArchive mergedResults = new EpsilonBoxDominanceArchive(epsilon);
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
}
