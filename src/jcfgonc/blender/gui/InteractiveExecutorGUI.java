package jcfgonc.blender.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;

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
	private Properties algorithmProperties;
	private Problem problem;
	private StatusPanel statusPanel;
	private OptimisationControlPanel optimisationControlPanel;
	private BarChartPanel timeEpochPanel;
	private JPanel upperPanel;
	private JPanel upperLeftPanel;
	private StepChartPanel ndsSizePanel;
	private ObjectivesLineChartPanel objectivesLineChartPanel;

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
		this.algorithmProperties = interactiveExecutor.getAlgorithmProperties();
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

		ndsSizePanel = new StepChartPanel("Size of the Non Dominated Set vs Epoch", "Epoch", "Size of the Non Dominated Set", new Color(0, 200, 100));
		upperLeftPanel.add(ndsSizePanel);

		objectivesLineChartPanel = new ObjectivesLineChartPanel("Minimum of objective vs Epoch", "irrelevant", "Value", problem);
		upperLeftPanel.add(objectivesLineChartPanel);

		technicalPanel = new JPanel();
		upperPanel.add(technicalPanel);
		technicalPanel.setLayout(new BoxLayout(technicalPanel, BoxLayout.Y_AXIS));

		statusPanel = new StatusPanel();
		technicalPanel.add(statusPanel);

		optimisationControlPanel = new OptimisationControlPanel(this);
		technicalPanel.add(optimisationControlPanel);

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

		statusPanel.getVariablesStatus().setText(Integer.toString(numberOfVariables));
		statusPanel.getObjectivesStatus().setText(Integer.toString(numberOfObjectives));
		statusPanel.getConstraintsStatus().setText(Integer.toString(numberOfConstraints));
		statusPanel.getPopulationSizeStatus().setText(algorithmProperties.getProperty("populationSize"));
		statusPanel.getAlgorithmStatus().setText(interactiveExecutor.getAlgorithmName());
		statusPanel.getMaxEpochsStatus().setText(Integer.toString(interactiveExecutor.getMaxEpochs()));
		statusPanel.getMaxRunsStatus().setText(Integer.toString(interactiveExecutor.getMaxRuns()));

		nonDominatedSetPanel.initialize();
//		timeEpochPanel.initialize(new Color(255, 106, 181));
//		ndsSizePanel.initialize(new Color(83, 255, 169));
		timeEpochPanel.initialize();
		ndsSizePanel.initialize();
		objectivesLineChartPanel.initialize();

//		this.setLocationRelativeTo(null); // center jframe

//		setLocation(-6, 0);
		windowResized(null);
		pack();
//		setPreferredSize(new Dimension(1920, 512));
//		setMinimumSize(new Dimension(800, 640));
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}

	/**
	 * Update the GUI. If the given NondominatedPopulation is null or empty this function just updates the epoch and run JLabels.
	 * 
	 * @param nds
	 * @param epoch
	 * @param run
	 * @param epochDuration
	 * @param objectiveMinimuns
	 */
	public void updateStatus(NondominatedPopulation nds, int epoch, int run, double epochDuration, double[] objectiveMinimuns) {
		statusPanel.getEpochStatus().setText(Integer.toString(epoch));
		statusPanel.getRunStatus().setText(Integer.toString(run));
		statusPanel.getLastEpochDuration().setText(String.format("%.3f", epochDuration));
		if (nds != null && !nds.isEmpty()) {
			statusPanel.getNdsSizeStatus().setText(Integer.toString(nds.size()));
			nonDominatedSetPanel.updateGraphs(nds);
			ndsSizePanel.addSample(epoch, nds.size());
		}
		if (epoch > 0) {
			timeEpochPanel.addSample(epoch, epochDuration, "duration");
		}
		if (objectiveMinimuns != null && objectiveMinimuns.length > 0) {
			objectivesLineChartPanel.addValue(objectiveMinimuns, epoch);
		}
	}

	protected void windowResized(ComponentEvent e) {
		// position horizontal divider to give space to the right pane
		// horizontalPane.setDividerLocation(horizontalPane.getWidth() - rightPanel.getMinimumSize().width);
//		int uw = upperPanel.getWidth();
//		int rw = rightPanel.getWidth();
//		ndsPanel.setMaximumSize(new Dimension(uw - rw, Integer.MAX_VALUE));
	}

	/**
	 * from https://stackoverflow.com/a/30335948
	 */
	public void saveScreenShot(String filename) {
		JComponent yourComponent = contentPane;
		BufferedImage img = new BufferedImage(yourComponent.getWidth(), yourComponent.getHeight(), BufferedImage.TYPE_INT_RGB);
		yourComponent.paint(img.getGraphics());
		File outputfile = new File(filename);
		try {
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	public void clearGraphs() {
		nonDominatedSetPanel.clearData();
		ndsSizePanel.clearData();
		timeEpochPanel.clearData();
		objectivesLineChartPanel.clearData();
	}
}
