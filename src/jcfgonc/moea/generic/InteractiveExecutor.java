package jcfgonc.moea.generic;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.UIManager;

import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.spi.AlgorithmFactory;

import jcfgonc.moea.specific.ResultsWriter;
import structures.Ticker;
import utils.VariousUtils;

public class InteractiveExecutor {
	private String algorithmName;
	private Properties algorithmProperties;
	private int maxEpochs;
	private NondominatedPopulation lastResult;
	private boolean canceled;
	private boolean skipCurrentRun;
	private Problem problem;
	private InteractiveExecutorGUI gui;
	@SuppressWarnings("unused")
	private final DecimalFormat ss_filename_df = new DecimalFormat("000");
	private ResultsWriter resultsWriter;
	private String resultsFilename;
	private int maxRuns;
//	private BlenderVisualizer blenderVisualizer;

	public InteractiveExecutor(Problem problem, String algorithmName, Properties algorithmProperties, int maxEpochs, int populationSize, int maxRuns,
			String resultsFilename, ResultsWriter rw) {
		this.problem = problem;
		this.algorithmName = algorithmName;
		this.algorithmProperties = algorithmProperties;
		this.maxEpochs = maxEpochs;
		this.maxRuns = maxRuns;
		this.resultsWriter = rw;
		this.resultsFilename = resultsFilename;
//		this.blenderVisualizer = new BlenderVisualizer(populationSize);
		this.gui = new InteractiveExecutorGUI(this);
		this.gui.initializeTheRest();
		this.gui.setVisible(true);
	}

	public NondominatedPopulation execute(int moea_run) throws InterruptedException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// couldn't set system look and feel, continue with default
		}

		// code for external evolution visualizer
//		new Thread() {
//			public void run() {
//				try {
//					blenderVisualizer.execute();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			};
//		}.start();

		int epoch = 0;
		Algorithm algorithm = null;
		lastResult = null;

		algorithm = AlgorithmFactory.getInstance().getAlgorithm(algorithmName, algorithmProperties, problem);
		canceled = false;
		skipCurrentRun = false;
		Ticker ticker = new Ticker();

		gui.updateStatus(lastResult, epoch, moea_run);

		do {

			ticker.resetTicker();
			algorithm.step();
			System.out.format("algorithm.step() %d took %f seconds\n", epoch, ticker.getTimeDeltaLastCall());

			lastResult = algorithm.getResult();

			// update graphs
			gui.updateStatus(lastResult, epoch, moea_run);

			// gui.saveScreenShot("screenshots/" + ss_filename_df.format(moea_run) + "_" + ss_filename_df.format(epoch) + ".png");
			// calculateMinimumOfObjectives(accumulatedResults, problem.getNumberOfObjectives());

			// update blender visualizer
//			blenderVisualizer.update(lastResult);
			if (algorithm.isTerminated() || epoch >= maxEpochs || canceled || skipCurrentRun) {
				break; // break while loop
			}
			epoch++;
		} while (true);

		algorithm.terminate();
		return lastResult;
	}

	public void closeGUI() {
		gui.dispose();
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

	public int getMaxEpochs() {
		return maxEpochs;
	}

	public NondominatedPopulation getLastResult() {
		return lastResult;
	}

	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Called when the user clicks on the abort button. Saves last results and exits the JVM.
	 */
	public void abortOptimization() {
		resultsWriter.appendResultsToFile(resultsFilename, lastResult, problem);
		resultsWriter.close();
		System.exit(-1);
	}

	/**
	 * called when the user clicks on the stop button
	 */
	public void stopOptimization() {
		// stop MOEA's loop
		this.canceled = true; // and stops the caller
	}

	public void skipCurrentRun() {
		this.skipCurrentRun = true;
	}

	@SuppressWarnings("unused")
	private void calculateMinimumOfObjectives(NondominatedPopulation nds, int numberOfObjectives) {
		double[] minimums = new double[numberOfObjectives];
		Arrays.fill(minimums, Double.MAX_VALUE);

		// for each objective
		for (int objective_i = 0; objective_i < numberOfObjectives; objective_i++) {
			// get the minimum in the current solution set
			for (int solution_i = 0; solution_i < nds.size(); solution_i++) {
				Solution solution = nds.get(solution_i);
				double val = solution.getObjective(objective_i);
				if (val < minimums[objective_i]) {
					minimums[objective_i] = val;
				}
			}
		}
		VariousUtils.printArray(minimums);
	}

	public int getMaxRuns() {
		return maxRuns;
	}
}
