package jcfgonc.blender;

import java.math.BigInteger;

public class BlenderMoConfig {
	public static final String ALGORITHM = "NSGAIII";
	public static final int POPULATION_SIZE = 256;
	public static final int MAX_EPOCHS = 256; // maximum number of epochs/generations to iterate
	public static final int MOEA_RUNS = 128; // maximum number of MOEA runs (each run iterates max_epochs)

	public static final double EDGE_MUTATION_PROBABILITY_POWER = 4;
	public static final double EDGE_MUTATION_NUMBER_STEPS = 1; // it goes from 1 to this number

	public static final int BLOCK_SIZE = 256; // querykb tool specific
	public static final int PARALLEL_LIMIT = 1; // number of threads for the querykb tool
	public static final BigInteger SOLUTION_LIMIT = BigInteger.valueOf(Long.MAX_VALUE);
	public static final long QUERY_TIMEOUT_SECONDS = Long.MAX_VALUE;

	public static final String inputSpacePath = "data/conceptnet5v5.csv";
	public static final String mappingPath = "data/2020-04-29_21-23-37_mappings.csv";
	public static final String framesPath = "data/pattern_resultsV22.tsv";

	public static final String frameSimilarityFilename = "data/patterns_semantic_similarityV22.tsv";
	public static final String wordembedding_filename = "D:\\\\Temp\\\\ontologies\\\\word emb\\\\ConceptNet Numberbatch 19.08\\\\numberbatch-en.txt";
	public static final String synonyms_filename = "data/synonyms.txt";
	public static final String wordPairScores_filename = "data/relation_pair_scores.tsv";

	public static final String vitalRelationsPath = "data/vital_relations.tsv";
}
