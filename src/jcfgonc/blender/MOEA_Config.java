package jcfgonc.blender;

public class MOEA_Config {
	public static final String ALGORITHM = "eNSGAII";
	public static int POPULATION_SIZE = 2048;
	public static int MAX_EPOCHS = 99999; // maximum number of epochs/generations to iterate
	public static int MOEA_RUNS = 99999; // maximum number of MOEA runs (each run iterates max_epochs)
	public static double MAX_RUN_TIME = 240.0; // maximum amount of time (minutes) allowed for each MOEA run

	public static double EDGE_MUTATION_PROBABILITY_POWER = 4;
	public static double EDGE_MUTATION_NUMBER_STEPS = 1; // it goes from 1 to this number

	public static final String inputSpacePath = "data/conceptnet5v44.csv";
	public static final String mappingPath = "data/2020-04-29_21-23-37_mappings.csv";

	public static final String framesPath = "data/pattern_resultsV3.tsv";
	public static final String frameSimilarityFilename = "data/patterns_semantic_similarityV22.tsv";

	public static final String wordembedding_filename = "D:\\\\Temp\\\\ontologies\\\\word emb\\\\ConceptNet Numberbatch 19.08\\\\numberbatch-en.txt";
	public static final String synonyms_filename = "data/synonyms.txt";
	public static final String wordPairScores_filename = "data/relation_pair_scores.tsv";

	public static final String vitalRelationsPath = "data/vital_relations.tsv";
	public static final String screenshotsFolder = "screenshots";
	public static final boolean GRAPHS_ENABLED = true;
	public static final boolean SCREENSHOTS_ENABLED = false;
	public static final boolean LAST_EPOCH_SCREENSHOT = true;
}
