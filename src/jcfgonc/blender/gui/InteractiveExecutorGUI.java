package jcfgonc.blender.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import jcfgonc.blender.MOEA_Config;
import jcfgonc.moea.generic.InteractiveExecutor;

public class InteractiveExecutorGUI extends JFrame {

	private static final long serialVersionUID = 5577378439253898247L;
	private JPanel contentPane;
	private NonDominatedSetPanel nonDominatedSetPanel;
	private JPanel technicalPanel;
	private InteractiveExecutor interactiveExecutor;
	private int numberOfVariables;
	private int numberOfObjectives;
	private int numberOfConstraints;
	private Problem problem;
	private StatusPanel statusPanel;
	private OptimisationControlPanel optimisationControlPanel;
	private BarChartPanel timeEpochPanel;
	private JPanel upperPanel;
	private JPanel upperLeftPanel;
	private BarChartPanel ndsSizePanel;
	private ObjectivesLineChartPanel objectivesLineChartPanel;
	private SettingsPanel settingsPanel;
	private JPanel fillPanel;
	private final DecimalFormat screenshotFilenameDecimalFormat = new DecimalFormat("0000");

	/**
	 * Create the frame.
	 * 
	 * @param properties
	 * @param interactiveExecutor
	 * 
	 * @param k
	 * @param j
	 * @param i
	 */
	public InteractiveExecutorGUI(InteractiveExecutor interactiveExecutor) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				abortOptimization();
			}
		});
		this.interactiveExecutor = interactiveExecutor;
		this.problem = interactiveExecutor.getProblem();
		this.numberOfVariables = problem.getNumberOfVariables();
		this.numberOfObjectives = problem.getNumberOfObjectives();
		this.numberOfConstraints = problem.getNumberOfConstraints();
		initialize();
	}

	private void initialize() {
		setTitle("Blender 2.0 - Multiple Objective Optimization");
		setName("MOEA");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(2, 0, 0, 0));

		upperPanel = new JPanel();
		contentPane.add(upperPanel);
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));

		upperLeftPanel = new JPanel();
		upperPanel.add(upperLeftPanel);
		upperLeftPanel.setLayout(new GridLayout(1, 0, 0, 0));

		timeEpochPanel = new BarChartPanel("Time vs Epoch", "Epoch", "Time (s)", new Color(200, 0, 100));
		upperLeftPanel.add(timeEpochPanel);

		ndsSizePanel = new BarChartPanel("Size of the Non Dominated Set vs Epoch", "Epoch", "Size of the Non Dominated Set", new Color(0, 200, 100));
		upperLeftPanel.add(ndsSizePanel);

		objectivesLineChartPanel = new ObjectivesLineChartPanel("Minimum of objective vs Epoch", "irrelevant", "Value", problem);
		upperLeftPanel.add(objectivesLineChartPanel);

		technicalPanel = new JPanel();
		upperPanel.add(technicalPanel);
		technicalPanel.setLayout(new BoxLayout(technicalPanel, BoxLayout.Y_AXIS));

		statusPanel = new StatusPanel();
		technicalPanel.add(statusPanel);

		settingsPanel = new SettingsPanel();
		technicalPanel.add(settingsPanel);

		optimisationControlPanel = new OptimisationControlPanel(this);
		technicalPanel.add(optimisationControlPanel);

		fillPanel = new JPanel();
		technicalPanel.add(fillPanel);
		fillPanel.setLayout(new BorderLayout(0, 0));

		nonDominatedSetPanel = new NonDominatedSetPanel(problem, Color.BLACK);
		contentPane.add(nonDominatedSetPanel);

		addComponentListener(new ComponentAdapter() { // window resize event
			@Override
			public void componentResized(ComponentEvent e) {
				windowResized(e);
			}
		});
	}

	public void abortOptimization() {
		// default icon, custom title
		int n = JOptionPane.showConfirmDialog(null, "Aborting optimization will discard the results of the current epoch.\nAre you sure?",
				"Abort Optimization", JOptionPane.YES_NO_OPTION);
		if (n != 0)
			return;
		setVisible(false);
		interactiveExecutor.abortOptimization();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@SuppressWarnings("unused")
	private int proportionOfInt(int value, double proportion) {
		double newval = (double) value * proportion;
		return (int) newval;
	}

	/**
	 * contains the rest of the stuff which cannot be initialized in the initialize function (because of the windowbuilder IDE)
	 */
	public void initializeTheRest() {
		ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
		// ChartFactory.setChartTheme(StandardChartTheme.createDarknessTheme());

		nonDominatedSetPanel.initialize();
//		timeEpochPanel.initialize(new Color(255, 106, 181));
//		ndsSizePanel.initialize(new Color(83, 255, 169));
		timeEpochPanel.initialize();
		ndsSizePanel.initialize();
		objectivesLineChartPanel.initialize();

//		this.setLocationRelativeTo(null); // center jframe

		settingsPanel.setNumberEpochs(MOEA_Config.MAX_EPOCHS);
		settingsPanel.setNumberRuns(MOEA_Config.MOEA_RUNS);
		settingsPanel.setPopulationSize(MOEA_Config.POPULATION_SIZE);

		statusPanel.initializedTimeCounters();

//		setLocation(-6, 0);
		windowResized(null);
		pack();
//		setPreferredSize(new Dimension(1920, 512));
//		setMinimumSize(new Dimension(800, 640));
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}

	private double[] calculateMinimumOfObjectives(NondominatedPopulation nds) {
		int numberOfObjectives = problem.getNumberOfObjectives();
		double[] minimums = new double[numberOfObjectives];
		Arrays.fill(minimums, Double.MAX_VALUE);

		// get the minimum in the current solution set
		for (int solution_i = 0; solution_i < nds.size(); solution_i++) {
			Solution solution = nds.get(solution_i);
			// for each objective
			for (int objective_i = 0; objective_i < numberOfObjectives; objective_i++) {
				double val = solution.getObjective(objective_i);
				if (val < minimums[objective_i]) {
					minimums[objective_i] = val;
				}
			}
		}
//		VariousUtils.printArray(minimums);
		return minimums;
	}

	/**
	 * Update the GUI. If the given NondominatedPopulation is null or empty this function just updates the epoch and run JLabels.
	 * 
	 * @param nds
	 * @param epoch
	 * @param run
	 * @param epochDuration
	 */
	public void updateStatus(NondominatedPopulation nds, int epoch, int run, double epochDuration) {

		statusPanel.setNumberRuns(Integer.toString(MOEA_Config.MOEA_RUNS));
		statusPanel.setNumberEpochs(Integer.toString(MOEA_Config.MAX_EPOCHS));
//		statusPanel.setPopulationSize(Integer.toString(MOEA_Config.POPULATION_SIZE));
		statusPanel.setPopulationSize(getAlgorithmProperties().getProperty("populationSize")); // get directly from the algorithm's properties

		statusPanel.setEpoch(Integer.toString(epoch));
		statusPanel.setCurrentRun(Integer.toString(run));
		statusPanel.setVariables(Integer.toString(numberOfVariables));
		statusPanel.setObjectives(Integer.toString(numberOfObjectives));
		statusPanel.setConstraints(Integer.toString(numberOfConstraints));
		statusPanel.setAlgorithm(MOEA_Config.ALGORITHM);

		// the first epoch has two calls, the first to initialize status (nds=null)
		// the second with the results (nds) from the 0'th epoch
		if (nds != null) {
			nonDominatedSetPanel.updateGraphs(nds);
			statusPanel.setNDS_Size(Integer.toString(nds.size()));
			statusPanel.setLastEpochDuration(epochDuration);
		}

		if (settingsPanel.isPerformanceGraphsEnabled()) {
			if (nds != null) {
				timeEpochPanel.addSample(epoch, epochDuration);
				ndsSizePanel.addSample(epoch, nds.size());
				double[] objectiveMinimuns = calculateMinimumOfObjectives(nds);
				objectivesLineChartPanel.addValue(objectiveMinimuns, epoch);
			}
		}

		if (settingsPanel.isScreenshotsEnabled()) {
			new File(MOEA_Config.screenshotsFolder).mkdir();
			String filename = String.format("run_%s_epoch_%s", screenshotFilenameDecimalFormat.format(run),
					screenshotFilenameDecimalFormat.format(epoch));
			saveScreenShotPNG(MOEA_Config.screenshotsFolder + File.separator + filename + ".png");
		}
	}

	public void clearGraphs() {
		nonDominatedSetPanel.clearData();
		ndsSizePanel.clearData();
		timeEpochPanel.clearData();
		objectivesLineChartPanel.clearData();
	}

	protected void windowResized(ComponentEvent e) {
		// position horizontal divider to give space to the right pane
		// horizontalPane.setDividerLocation(horizontalPane.getWidth() - rightPanel.getMinimumSize().width);
//		int uw = upperPanel.getWidth();
//		int rw = rightPanel.getWidth();
//		ndsPanel.setMaximumSize(new Dimension(uw - rw, Integer.MAX_VALUE));
	}

	/**
	 * Saves the entire GUI to a file. Filename contains the extension/file format.
	 */
	public void saveScreenShotPNG(String filename) {
		JComponent yourComponent = contentPane;
		BufferedImage img = new BufferedImage(yourComponent.getWidth(), yourComponent.getHeight(), BufferedImage.TYPE_INT_RGB);
		yourComponent.paint(img.getGraphics());

		GUI_Utils.saveScreenShotPNG(img, filename);
	}

	public void stopOptimization() {
		interactiveExecutor.stopOptimization();
	}

	public void printNonDominatedSet() {
		nonDominatedSetPanel.printNonDominatedSet();
	}

	public void skipCurrentRun() {
		interactiveExecutor.skipCurrentRun();
	}

	public void debug() {
		System.out.println(this.getLocation());
		System.out.println(this.getSize());
	}

	public Properties getAlgorithmProperties() {
		return interactiveExecutor.getAlgorithmProperties();
	}

	public void resetCurrentRunTime() {
		statusPanel.resetCurrentRunTime();
	}
}
