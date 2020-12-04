package jcfgonc.blender;

import java.math.BigInteger;

public class BlenderMoConfig {
	public static final int POPULATION_SIZE = 1024;
	public static final int BLOCK_SIZE = 256; // querykb tool specific
	public static final int PARALLEL_LIMIT = 1; // number of threads for the querykb tool
	public static final BigInteger SOLUTION_LIMIT = BigInteger.valueOf(Long.MAX_VALUE);
	public static final double EDGE_MUTATION_PROBABILITY_POWER = 3;
	public static final double EDGE_MUTATION_NUMBER_STEPS = 5; // it goes from 1 to this number+1
	public static final long QUERY_TIMEOUT_SECONDS = Long.MAX_VALUE;
}
