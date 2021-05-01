package jcfgonc.moea.generic;

import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.spi.AlgorithmFactory;

import jcfgonc.blender.MOEA_Config;
import jcfgonc.blender.gui.InteractiveExecutorGUI;
import jcfgonc.moea.specific.ResultsWriter;
import structures.Ticker;

public class InteractiveExecutor {
	private Properties algorithmProperties;
	private NondominatedPopulation lastResult;
	/**
	 * cancels the MOEA in general
	 */
	private boolean canceled;
	/**
	 * cancels/skips the current run and jumps to the next
	 */
	private boolean skipCurrentRun;
	private Problem problem;
	private InteractiveExecutorGUI gui;
	private ResultsWriter resultsWriter;
	private String resultsFilename;
//	private BlenderVisualizer blenderVisualizer;

	public InteractiveExecutor(Problem problem, Properties algorithmProperties, String resultsFilename, ResultsWriter rw) {
		this.problem = problem;
		this.algorithmProperties = algorithmProperties;
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

		algorithm = AlgorithmFactory.getInstance().getAlgorithm(MOEA_Config.ALGORITHM, algorithmProperties, problem);
		canceled = false;
		skipCurrentRun = false;
		Ticker ticker = new Ticker();

		clearGraphs();
		gui.resetCurrentRunTime();
//		gui.updateStatus(null, epoch, moea_run, 0);

		ticker.resetTicker();
		do {
			// count algorithm time
			algorithm.step();
			double epochDuration = ticker.getTimeDeltaLastCall();

			lastResult = algorithm.getResult();

			// update GUI stuff
			updateStatus(moea_run, epoch, epochDuration);

			// update blender visualizer
//			blenderVisualizer.update(lastResult);
			double runElapsedTime = ticker.getElapsedTime() / 60.0;
//			System.out.println(runElapsedTime);
			if (algorithm.isTerminated() || //
					runElapsedTime > MOEA_Config.MAX_RUN_TIME || //
					epoch >= MOEA_Config.MAX_EPOCHS || //
					canceled || //
					skipCurrentRun) {
				break; // break while loop
			}
			epoch++;
		} while (true);

		algorithm.terminate();
		gui.takeLastEpochScreenshot();
		return lastResult;
	}

	private void updateStatus(int moea_run, int epoch, double epochDuration) {
		Runnable updater = new Runnable() {
			public void run() {
				try {
					gui.updateData(lastResult, epoch, moea_run, epochDuration);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
			}
		};
		SwingUtilities.invokeLater(updater);
	}

	private void clearGraphs() {
		Runnable updater = new Runnable() {
			public void run() {
				try {
					gui.clearGraphs();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
			}
		};
		SwingUtilities.invokeLater(updater);
	}

	public void closeGUI() {
		gui.dispose();
	}

	public Problem getProblem() {
		return problem;
	}

	public Properties getAlgorithmProperties() {
		return algorithmProperties;
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
		gui.takeLastEpochScreenshot();
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
}
