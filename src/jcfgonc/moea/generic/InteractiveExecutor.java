package jcfgonc.moea.generic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.swing.UIManager;

import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.spi.AlgorithmFactory;

import graph.StringGraph;
import jcfgonc.moea.specific.CustomChromosome;
import utils.VariousUtils;

public class InteractiveExecutor {
	private String algorithmName;
	private Properties algorithmProperties;
	private int maxGenerations;
	private NondominatedPopulation lastResult;
	private boolean canceled;
	private Problem problem;
//	private BlenderVisualizer blenderVisualizer;

	public InteractiveExecutor(Problem problem, String algorithmName, Properties algorithmProperties, int maxGenerations, int populationSize) {
		this.problem = problem;
		this.algorithmName = algorithmName;
		this.algorithmProperties = algorithmProperties;
		this.maxGenerations = maxGenerations;
//		this.blenderVisualizer = new BlenderVisualizer(populationSize);
	}

	public NondominatedPopulation execute() throws InterruptedException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// couldn't set system look and feel, continue with default
		}

		InteractiveExecutorGUI gui = new InteractiveExecutorGUI(this);
//		new Thread() {
//			public void run() {
//				try {
//					blenderVisualizer.execute();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			};
//		}.start();

		int generation = 0;
		Algorithm algorithm = null;
		lastResult = null;

//		algorithm = AlgorithmFactory.getInstance().getAlgorithm(algorithmName, algorithmProperties, distributedProblem);
		algorithm = AlgorithmFactory.getInstance().getAlgorithm(algorithmName, algorithmProperties, problem);
		gui.initializeTheRest();
		gui.setVisible(true);
		this.canceled = false;

		do {
			// update graphs
			gui.updateStatus(lastResult, generation);

			algorithm.step();
			generation++;
			lastResult = algorithm.getResult();

			// update blender visualizer
//			blenderVisualizer.update(lastResult);
			if (algorithm.isTerminated() || generation >= maxGenerations || this.canceled) {
				break; // break while loop
			}
		} while (true);

		algorithm.terminate();
		gui.dispose();
		showAndSaveLastResult();
		return lastResult;
	}

	public Problem getProblem() {
		return problem;
	}

	public String getAlgorithmName() {
		return algorithmName;
	}

	public Properties getAlgorithmProperties() {
		return algorithmProperties;
	}

	public int getMaxGenerations() {
		return maxGenerations;
	}

	public NondominatedPopulation getLastResult() {
		return lastResult;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void showAndSaveLastResult() {
		if (lastResult == null) {
			System.out.println("no results to show");
			return;
		}
		try {
			saveResultsFile(String.format("moea_results_%s.tsv", VariousUtils.generateCurrentDateAndTimeStamp()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// TODO this is hard-coded for the blender
	// TODO support multiple variables
	private void saveResultsFile(String filename) throws IOException {
		int numberOfObjectives = lastResult.iterator().next().getNumberOfObjectives();

		File file = new File(filename);
		FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8);
		BufferedWriter bw = new BufferedWriter(fw);

		ProblemDescription pd = (ProblemDescription) problem;
		// write header
		// write header
		for (int oi = 0; oi < numberOfObjectives; oi++) {
			String objectiveDescription = pd.getObjectiveDescription(oi);
			bw.write(String.format("%s\t", objectiveDescription));
		}
		// graph data header
		bw.write("d:graph's vertices\t");
		bw.write("d:graph's edges\t");
		bw.write(pd.getVariableDescription(0));
		bw.newLine();

		// write data
		// write data
		for (Solution solution : lastResult) {
			// write objectives
			for (int i = 0; i < numberOfObjectives; i++) {
				bw.write(Double.toString(solution.getObjective(i)));
				bw.write('\t');
			}
			
			// graph data
			CustomChromosome cc = (CustomChromosome) solution.getVariable(0); // unless the solution domain X has more than one dimension
			StringGraph blendSpace = cc.getBlend().getBlendSpace();

			bw.write(String.format("%d\t", blendSpace.numberOfVertices()));
			bw.write(String.format("%d\t", blendSpace.numberOfEdges()));
			bw.write(String.format("%s", blendSpace));
			bw.newLine();
		}
		bw.close();
		fw.close();
	}

	/**
	 * called when the user clicks on the abort button
	 */
	public void abortOptimization() {
		// show last non dominated set and terminate execution
		showAndSaveLastResult();
		System.exit(-1);
	}

	/**
	 * called when the user clicks on the stop button
	 */
	public void stopOptimization() {
		// stop MOEA's loop
		this.canceled = true;
	}
}
