package jcfgonc.blender;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.Well44497b;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.core.spi.OperatorProvider;
import org.moeaframework.util.TypedProperties;

import com.githhub.aaronbembenek.querykb.Query;

import frames.FrameReadWrite;
import frames.SemanticFrame;
import graph.StringGraph;
import jcfgonc.blender.logic.FileTools;
import jcfgonc.blender.logic.LogicUtils;
import jcfgonc.moea.generic.InteractiveExecutor;
import jcfgonc.moea.specific.CustomMutation;
import jcfgonc.moea.specific.CustomProblem;
import structures.Mapping;

public class BlenderMoLauncher {
	@SuppressWarnings("unused")
	public static void main(String[] args) throws NoSuchFileException, IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		RandomAdaptor random = new RandomAdaptor(new Well44497b());

		String inputSpacePath = "../ConceptNet5/kb/conceptnet5v5.csv";
		String mappingPath = "../EEmapper/2020-04-29_21-23-37_mappings.csv";
		String framesPath = "../PatternMiner/results/resultsV22.csv";

		String frameSimilarityFilename = "..\\PatternMiner\\results\\patterns_semantic_similarityV22.tsv";
		String synonyms_filename = "C:\\Desktop\\github\\my source code\\PatternMiner\\results\\synonyms.txt";
		String wordembedding_filename = "D:\\\\Temp\\\\ontologies\\\\word emb\\\\ConceptNet Numberbatch 19.08\\\\numberbatch-en.txt";

		// read input space
		StringGraph inputSpace = FileTools.readInputSpace(inputSpacePath);

		// read mappings file (contains multiple mappings)
		ArrayList<Mapping<String>> mappings = FileTools.readMappings(mappingPath);

		// read frames file
		ArrayList<SemanticFrame> frames0 = FrameReadWrite.readPatternFrames(framesPath);
		FrameReadWrite.updatePatternFrameSimilarity(frames0, frameSimilarityFilename);
		// filter some frames
		ArrayList<SemanticFrame> frames = new ArrayList<SemanticFrame>(64 * 1024);
		for (SemanticFrame frame : frames0) {
			if (frame.getGraph().numberOfEdges() > 10)
				continue;
			if (frame.getMatches() < 1)
				continue;
			if (frame.getRelationTypesStd() > 1)
				continue;
			frames.add(frame);
		}
		Collections.shuffle(frames, random);
		// frames = new ArrayList<SemanticFrame>(frames.subList(0, 1024));
		System.out.printf("using %d frames\n", frames.size());
		frames0 = null;

		// create frame queries
		ArrayList<Query> frameQueries = new ArrayList<>(frames.size());
		for (int i = 0; i < frames.size(); i++) {
			SemanticFrame frame = frames.get(i);
			StringGraph g = frame.getGraph();
			Query q = LogicUtils.createQueryFromStringGraph(g);
			frameQueries.add(q);
		}

//		ListWordEmbedding we = WordEmbeddingReadWrite.readCSV(wordembedding_filename, true);
//		MapOfList<String, String> synonyms = WordEmbeddingUtils.readSynonymWordList(synonyms_filename, we);
//		Object2DoubleOpenHashMap<UnorderedPair<String>> wps = WordEmbeddingUtils.scoreWordPairs(we, synonyms);

		// // test the mutation using a custom GUI
		// TestMutation.testMutation(inputSpace, mappings);

		// setup the mutation and the MOEA
		registerCustomMutation();
		Properties properties = new Properties();
		properties.setProperty("operator", "CustomMutation");
		properties.setProperty("CustomMutation.Rate", Double.toString(1.0));
		properties.setProperty("populationSize", Integer.toString(BlenderMoConfig.POPULATION_SIZE));

		BlendMutation.setInputSpace(inputSpace);
		BlendMutation.setRandom(random);
		// TODO: personalize your constructor here
		CustomProblem problem = new CustomProblem(inputSpace, mappings, frames, frameQueries, random);
//		problem.setWordPairsSemanticSimilarity(wps);
		InteractiveExecutor ie = new InteractiveExecutor(problem, "NSGAII", properties, Integer.MAX_VALUE);
		@SuppressWarnings("unused")
		NondominatedPopulation np = ie.execute();

	}

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
}
