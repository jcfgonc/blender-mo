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
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import jcfgonc.blender.logic.FileTools;
import jcfgonc.blender.logic.LogicUtils;
import jcfgonc.moea.generic.InteractiveExecutor;
import jcfgonc.moea.specific.CustomMutation;
import jcfgonc.moea.specific.CustomProblem;
import structures.Mapping;
import structures.Ticker;
import structures.UnorderedPair;
import wordembedding.WordEmbeddingUtils;

public class BlenderMoLauncher {
	@SuppressWarnings("unused")
	public static void main(String[] args) throws NoSuchFileException, IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		Ticker ticker = new Ticker();
		RandomAdaptor random = new RandomAdaptor(new Well44497b());

		// read input space
		StringGraph inputSpace = FileTools.readInputSpace(BlenderMoConfig.inputSpacePath);

		// read mappings file (contains multiple mappings)
		ArrayList<Mapping<String>> mappings = FileTools.readMappings(BlenderMoConfig.mappingPath);

		// read frames file
		ArrayList<SemanticFrame> frames0 = FrameReadWrite.readPatternFrames(BlenderMoConfig.framesPath);
		FrameReadWrite.updatePatternFrameSimilarity(frames0, BlenderMoConfig.frameSimilarityFilename);
		// filter some frames
		ArrayList<SemanticFrame> frames = new ArrayList<SemanticFrame>(64 * 1024);
		for (SemanticFrame frame : frames0) {
			if (frame.getGraph().numberOfEdges() > 10)
				continue;
			if (frame.getMatches() < 3)
				continue;
			if (frame.getRelationTypesStd() > 0.5)
				continue;
			frames.add(frame);
		}
		Collections.shuffle(frames, random);
		// frames = new ArrayList<SemanticFrame>(frames.subList(0, 1024));
		System.out.printf("using %d frames\n", frames.size());
		frames0 = null;

		// create frame queries
		ticker.getTimeDeltaLastCall();
		ArrayList<Query> frameQueries = new ArrayList<>(frames.size());
		for (int i = 0; i < frames.size(); i++) {
			SemanticFrame frame = frames.get(i);
			StringGraph g = frame.getGraph();
			Query q = LogicUtils.createQueryFromStringGraph(g);
			frameQueries.add(q);
		}
		System.out.println("took " + ticker.getTimeDeltaLastCall() + "s to create querykb's Queries");

		// read pre-calculated semantic scores of word/relation pairs
		Object2DoubleOpenHashMap<UnorderedPair<String>> wps = WordEmbeddingUtils.readWordPairScores(BlenderMoConfig.wordPairScores_filename);

		// read vital relations importance
		Object2DoubleOpenHashMap<String> vitalRelations = FileTools.readVitalRelations(BlenderMoConfig.vitalRelationsPath);

		// // test the mutation using a custom GUI
		// TestMutation.testMutation(inputSpace, mappings);

		// setup the mutation and the MOEA
		registerCustomMutation();
		Properties properties = new Properties();
		properties.setProperty("operator", "CustomMutation");
		properties.setProperty("CustomMutation.Rate", "1.0");
		properties.setProperty("populationSize", Integer.toString(BlenderMoConfig.POPULATION_SIZE));
		properties.setProperty("instances", "100");
		properties.setProperty("epsilon", "0.25");

		BlendMutation.setInputSpace(inputSpace);
		BlendMutation.setRandom(random);
		// TODO: personalize your constructor here
		CustomProblem problem = new CustomProblem(inputSpace, mappings, frames, frameQueries, vitalRelations, wps, random);

		InteractiveExecutor ie = new InteractiveExecutor(problem, BlenderMoConfig.ALGORITHM, properties, BlenderMoConfig.MAX_EPOCHS,
				BlenderMoConfig.POPULATION_SIZE);
		for (int moea_run = 0; moea_run < BlenderMoConfig.MOEA_RUNS; moea_run++) {
			if (ie.isCanceled())
				break;
			System.gc();
			if (moea_run > 1)
				Thread.sleep(10000); // give GC time to do its job
			NondominatedPopulation np = ie.execute(moea_run);
		}
		ie.closeGUI();

		// terminate daemon threads
		System.exit(0);
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
