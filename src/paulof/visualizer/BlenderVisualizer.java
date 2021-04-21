package paulof.visualizer;

import java.util.concurrent.LinkedBlockingQueue;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import graph.StringGraph;
import jcfgonc.blender.structures.Blend;
import jcfgonc.moea.specific.CustomChromosome;
import structures.Mapping;

public class BlenderVisualizer {

	private int populationSize;
	private LinkedBlockingQueue<NondominatedPopulation> generations;

	/**
	 * initializes internal structures and then returns (caller waits for this)
	 * 
	 * @param populationSize
	 */
	public BlenderVisualizer(int populationSize) {
		this.populationSize = populationSize;
		this.generations = new LinkedBlockingQueue<>();
		// TODO
	}

	/**
	 * invoked by the MOEA asynchronously to update the visualizer with the recently population
	 * 
	 * @param generation the last generation's id (always increasing)
	 * @param lastResult the MOEA's latest generation
	 * @throws InterruptedException
	 */
	public void update(NondominatedPopulation lastResult) throws InterruptedException {
		// this should be always true
		// assert lastResult.size() == populationSize;
		generations.put(lastResult);
		// TODO
		// it's possible this function is just that code above...
	}

	/**
	 * visualizer starts executing autonomously/concurrently here (using a dedicated execution thread)
	 * 
	 * @throws InterruptedException
	 */
	public void execute() throws InterruptedException {
		// TODO Auto-generated method stub
		while (true) {
			NondominatedPopulation gen = generations.take();
			System.out.println("BlenderVisualizer.execute() got " + gen);

			// code examples
			int populationSize = gen.size(); // size of the current population (should be the same every generation)
			Solution firstSolution = gen.get(0); // reference to the 0th solution of the generation
			double objective0 = firstSolution.getObjective(0); // the value of the 0th objective
			CustomChromosome variable = (CustomChromosome) firstSolution.getVariable(0); // MOEA specific
			Blend blend = variable.getBlend();
			
			// interesting blend stuff
			int blendId = blend.getId(); // my id
			int parentId = blend.getParentId(); // my father's id
			StringGraph blendSpace = blend.getBlendSpace(); // the blend space's graph
			Mapping<String> mapping = blend.getMapping(); // the mapping
		}
	}

}
