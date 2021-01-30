package jcfgonc.moea.generic;

import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.UIManager;

import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.spi.AlgorithmFactory;

public class InteractiveExecutor {
	private String algorithmName;
	private Properties algorithmProperties;
	private int maxGenerations;
	private NondominatedPopulation lastResult;
	private boolean canceled;
	private Problem problem;
	private InteractiveExecutorGUI gui;
//	private BlenderVisualizer blenderVisualizer;

	public InteractiveExecutor(Problem problem, String algorithmName, Properties algorithmProperties, int maxGenerations, int populationSize) {
		this.problem = problem;
		this.algorithmName = algorithmName;
		this.algorithmProperties = algorithmProperties;
		this.maxGenerations = maxGenerations;
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
		this.canceled = false;
		// Thread.sleep(3 * 60 * 1000);
//		Ticker t = new Ticker();

		do {
			// update graphs
			gui.updateStatus(lastResult, epoch, moea_run);

//			t.getTimeDeltaLastCall();
			algorithm.step();
//			double dt = t.getTimeDeltaLastCall();
//			System.out.format("epoch %d took %f seconds\n", epoch, dt);
			epoch++;
			lastResult = algorithm.getResult();

			// printscreen
			// gui.saveScreenShot("screenshots/" + VariousUtils.generateCurrentDateAndTimeStamp() + ".png");

			// update blender visualizer
//			blenderVisualizer.update(lastResult);
			if (algorithm.isTerminated() || epoch >= maxGenerations || this.canceled) {
				break; // break while loop
			}
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

	public int getMaxGenerations() {
		return maxGenerations;
	}

	public NondominatedPopulation getLastResult() {
		return lastResult;
	}

	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * called when the user clicks on the abort button
	 */
	public void abortOptimization() {
		System.exit(-1);
	}

	/**
	 * called when the user clicks on the stop button
	 */
	public void stopOptimization() {
		// stop MOEA's loop
		this.canceled = true;
	}

	public void debug(ActionListener actionListener) {
		System.out.println(gui.toString());
	}
}
