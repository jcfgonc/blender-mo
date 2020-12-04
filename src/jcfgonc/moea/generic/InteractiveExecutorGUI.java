package jcfgonc.moea.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import utils.OSTools;

public class InteractiveExecutorGUI extends JFrame {

	private static final long serialVersionUID = 5577378439253898247L;
	private JPanel contentPane;
	private JSplitPane horizontalPane;
	private JPanel ndsPanel;
	private JPanel settingsPanel;
	private JButton stopButton;
	private JPanel buttonsPanel;
	private JButton abortButton;
	private InteractiveExecutor interactiveExecutor;
	private JPanel statusPanel;
	private JLabel epochTitle;
	private JLabel evaluationsTitle;
	private int numberOfVariables;
	private int numberOfObjectives;
	private int numberOfConstraints;
	private JLabel variablesTitle;
	private JLabel objectivesTitle;
	private JLabel constraintsTitle;
	private JLabel epochLabel;
	private JLabel variablesLabel;
	private JLabel objectivesLabel;
	private JLabel constraintsLabel;
	private JLabel populationSizeTitle;
	private JLabel populationSizeLabel;
	private Properties algorithmProperties;
	private ArrayList<XYSeries> ndsSeries;
	private JLabel ndsSizeTitle;
	private JLabel ndsSizeLabel;
	private boolean reverseGraphsVertically;
	private boolean reverseGraphsHorizontally;
	private JCheckBox checkBoxReverseH;
	private JPanel timeoutPanel;
	private JCheckBox checkBoxReverseV;
	private int numberNDSGraphs;
	private Problem problem;
	private JPanel configPanel;
	private JSpinner spinner;
	private JLabel lblNewLabel;
	private JButton btnNewButton;
	private int generation;
	private NondominatedPopulation population;

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
		this.interactiveExecutor = interactiveExecutor;
		this.problem = interactiveExecutor.getProblem();
		this.algorithmProperties = interactiveExecutor.getAlgorithmProperties();
		this.numberOfVariables = problem.getNumberOfVariables();
		this.numberOfObjectives = problem.getNumberOfObjectives();
		this.numberOfConstraints = problem.getNumberOfConstraints();
		initialize();
	}

	private void initialize() {
		setPreferredSize(new Dimension(624, 416));
		setTitle("MOEA");
		setName("MOEA");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		horizontalPane = new JSplitPane();
		horizontalPane.setEnabled(false);
		horizontalPane.setContinuousLayout(true);
		horizontalPane.setMinimumSize(new Dimension(608, 432));
		horizontalPane.setPreferredSize(new Dimension(608, 432));
		horizontalPane.setBorder(null);
		contentPane.add(horizontalPane);

		ndsPanel = new JPanel();
		ndsPanel.setMinimumSize(new Dimension(64, 10));
		ndsPanel.setBorder(null);
		horizontalPane.setLeftComponent(ndsPanel);
		ndsPanel.setLayout(new GridLayout(0, 1, 0, 0));

		settingsPanel = new JPanel();
		settingsPanel.setMinimumSize(new Dimension(288, 10));
		settingsPanel.setBorder(null);
		horizontalPane.setRightComponent(settingsPanel);
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

		statusPanel = new JPanel();
		statusPanel.setBorder(new TitledBorder(null, "Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		settingsPanel.add(statusPanel);
		statusPanel.setLayout(new GridLayout(0, 2, 0, 0));

		variablesTitle = new JLabel("Variables: ");
		variablesTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(variablesTitle);

		variablesLabel = new JLabel("");
		variablesLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(variablesLabel);

		objectivesTitle = new JLabel("Objectives: ");
		objectivesTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(objectivesTitle);

		objectivesLabel = new JLabel("");
		objectivesLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(objectivesLabel);

		constraintsTitle = new JLabel("Constraints: ");
		constraintsTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(constraintsTitle);

		constraintsLabel = new JLabel("");
		constraintsLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(constraintsLabel);

		populationSizeTitle = new JLabel("Population Size: ");
		populationSizeTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(populationSizeTitle);

		populationSizeLabel = new JLabel("");
		populationSizeLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(populationSizeLabel);

		epochTitle = new JLabel("Epoch: ");
		epochTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(epochTitle);

		epochLabel = new JLabel("");
		epochLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(epochLabel);

		evaluationsTitle = new JLabel("Evaluations: ");
		evaluationsTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(evaluationsTitle);

		ndsSizeTitle = new JLabel("Non-Dominated Set Size: ");
		ndsSizeTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(ndsSizeTitle);

		ndsSizeLabel = new JLabel("");
		ndsSizeLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(ndsSizeLabel);

		configPanel = new JPanel();
		configPanel.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		settingsPanel.add(configPanel);
		configPanel.setLayout(new GridLayout(0, 1, 0, 0));

		checkBoxReverseH = new JCheckBox("Flip Results Horizontally");
		checkBoxReverseH.setHorizontalAlignment(SwingConstants.CENTER);
		checkBoxReverseH.setAlignmentX(Component.CENTER_ALIGNMENT);
		configPanel.add(checkBoxReverseH);

		checkBoxReverseV = new JCheckBox("Flip Results Vertically");
		checkBoxReverseV.setHorizontalAlignment(SwingConstants.CENTER);
		configPanel.add(checkBoxReverseV);

		timeoutPanel = new JPanel();
		configPanel.add(timeoutPanel);
		timeoutPanel.setBorder(null);
		FlowLayout fl_timeoutPanel = new FlowLayout(FlowLayout.CENTER, 5, 5);
		timeoutPanel.setLayout(fl_timeoutPanel);

		lblNewLabel = new JLabel("querykb timeout (s)");
		timeoutPanel.add(lblNewLabel);

		spinner = new JSpinner();
		spinner.setEnabled(false);
		spinner.addChangeListener(new ChangeListener() {
			@SuppressWarnings("unused")
			public void stateChanged(ChangeEvent e) {
				JSpinner mySpinner = (JSpinner) (e.getSource());
				SpinnerNumberModel snm = (SpinnerNumberModel) mySpinner.getModel();
				// MOConfig.QUERY_TIMEOUT_SECONDS = snm.getNumber().intValue();
				// System.out.println(MOConfig.QUERY_TIMEOUT_SECONDS);
			}
		});
		spinner.setPreferredSize(new Dimension(64, 20));
		spinner.setModel(new SpinnerNumberModel(Integer.valueOf(60), Integer.valueOf(1), null, Integer.valueOf(1)));
		timeoutPanel.add(spinner);
		checkBoxReverseV.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reverseGraphsVertically = !reverseGraphsVertically;
			}
		});
		checkBoxReverseH.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reverseGraphsHorizontally = !reverseGraphsHorizontally;
			}
		});

		buttonsPanel = new JPanel();
		buttonsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Optimization Control", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingsPanel.add(buttonsPanel);

		stopButton = new JButton("Stop Optimization");
		stopButton.setToolTipText("Waits for the current epoch to complete and returns the best results so far.");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopOptimization();
			}
		});
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		buttonsPanel.add(stopButton);
		stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		stopButton.setBorder(UIManager.getBorder("Button.border"));

		abortButton = new JButton("Abort Optimization");
		abortButton.setToolTipText("Aborts the optimization by discarding the current epoch's results and returns the best results so far.");
		abortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// System.out.println((double) (horizontalPane.getDividerLocation()) / (horizontalPane.getWidth() - horizontalPane.getDividerSize()));
				abortOptimization();
			}
		});
		abortButton.setBorder(UIManager.getBorder("Button.border"));
		abortButton.setAlignmentX(0.5f);
		buttonsPanel.add(abortButton);

		btnNewButton = new JButton("Test");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printPopulation();
			}
		});
		buttonsPanel.add(btnNewButton);

		addComponentListener(new ComponentAdapter() { // window resize event
			@Override
			public void componentResized(ComponentEvent e) {
				windowResized(e);
			}
		});
	}

	private void abortOptimization() {
		// default icon, custom title
		int n = JOptionPane.showConfirmDialog(null, "Aborting optimization will discard the results of the current epoch.\nAre you sure?",
				"Abort Optimization", JOptionPane.YES_NO_OPTION);
		if (n != 0)
			return;
		interactiveExecutor.abortOptimization();
	}

	private void stopOptimization() {
		interactiveExecutor.stopOptimization();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	private int proportionOfInt(int value, double proportion) {
		double newval = (double) value * proportion;
		return (int) newval;
	}

	/**
	 * contains the rest of the stuff which cannot be initialized in the initialize function (because of the windowbuilder IDE)
	 */
	public void initializeTheRest() {
		variablesLabel.setText(Integer.toString(numberOfVariables));
		objectivesLabel.setText(Integer.toString(numberOfObjectives));
		constraintsLabel.setText(Integer.toString(numberOfConstraints));
		populationSizeLabel.setText(algorithmProperties.getProperty("populationSize"));

		reverseGraphsHorizontally = false;
		reverseGraphsVertically = false;
		// sync controls with boolean vars
		checkBoxReverseH.setSelected(reverseGraphsHorizontally);
		checkBoxReverseV.setSelected(reverseGraphsVertically);

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		this.setLocationRelativeTo(null); // center jframe

		this.setSize(proportionOfInt(width, 0.333), proportionOfInt(height, 0.333));
		this.pack();

		numberNDSGraphs = (int) Math.ceil((double) numberOfObjectives / 2); // they will be plotted in pairs of objectives
		// ndsGraphs = new ArrayList<>();
		ndsSeries = new ArrayList<>();
		int objectiveIndex = 0; // for laying out axis' labels
		for (int i = 0; i < numberNDSGraphs; i++) {
			XYSeries xySeries = new XYSeries("untitled");
			XYSeriesCollection dataset = new XYSeriesCollection();
			dataset.addSeries(xySeries);
			ndsSeries.add(xySeries);

			String xAxisLabel;
			String yAxisLabel;
			if (problem instanceof ProblemDescription) {
				ProblemDescription pd = (ProblemDescription) problem;
				if (objectiveIndex < numberOfObjectives - 1) { // only one objective
					xAxisLabel = pd.getObjectiveDescription(objectiveIndex);
					yAxisLabel = pd.getObjectiveDescription(objectiveIndex + 1);
				} else { // more than two objectives to follow
					xAxisLabel = pd.getObjectiveDescription(0);
					yAxisLabel = pd.getObjectiveDescription(objectiveIndex);
				}
			} else {
				if (objectiveIndex < numberOfObjectives - 1) { // only one objective
					xAxisLabel = String.format("Objective %d", objectiveIndex);
					yAxisLabel = String.format("Objective %d", objectiveIndex + 1);
				} else { // more than two objectives to follow
					xAxisLabel = String.format("Objective %d", 0);
					yAxisLabel = String.format("Objective %d", objectiveIndex);
				}
			}
			objectiveIndex += 2;

			String title = null;// String.format("Non-Dominated Set %d", i);
			JFreeChart chart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, false, false, false);
//			JFreeChart chart = ChartFactory.createXYStepChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, false, false, false);

//			// color
//			XYPlot plot = chart.getXYPlot();
//			XYItemRenderer renderer = plot.getRenderer();
//			renderer.setSeriesPaint(0, Color.green);
//			// fill shapes
//			XYStepRenderer rend = (XYStepRenderer) renderer;
//			rend.setShapesFilled(true);

			ChartPanel chartPanel = new ChartPanel(chart, false);
			ndsPanel.add(chartPanel);

			// limit jframe size according to windows' high dpi scaling
			double w = getPreferredSize().getWidth() * OSTools.getScreenScale();
			double h = getPreferredSize().getHeight() * OSTools.getScreenScale();
			setMinimumSize(new Dimension((int) w, (int) h));
		}
	}

	public void updateStatus(NondominatedPopulation population, int generation) {
		this.generation = generation;
		this.population = population;

		updateWindow();
	}

	public void updateWindow() {
		epochLabel.setText(Integer.toString(generation));

		if (population == null)
			return;

		// update the non-dominated sets
		ndsSizeLabel.setText(Integer.toString(population.size()));

		int objectiveIndex = 0;
		// iterate the scatter plots (each can hold two objectives)
		for (XYSeries graph : ndsSeries) {
			// empty data series
			graph.clear();
			// iterate the solutions
			for (Solution solution : population) {
				// pairs of objectives
				double x;
				double y;
				if (objectiveIndex < numberOfObjectives - 1) {
					x = solution.getObjective(objectiveIndex);
					y = solution.getObjective(objectiveIndex + 1);
				} else {
					x = solution.getObjective(0);
					y = solution.getObjective(objectiveIndex);
				}
				if (reverseGraphsHorizontally) {
					x = -x;
				}
				if (reverseGraphsVertically) {
					y = -y;
				}
				graph.add(x, y);
			}

			objectiveIndex += 2;
		}
//		ndsPanel.repaint();
	}

	protected void windowResized(ComponentEvent e) {
		// position horizontal divider to give space to the right pane
		horizontalPane.setDividerLocation(horizontalPane.getWidth() - settingsPanel.getMinimumSize().width);
	}

	private void printPopulation() {
		if (population == null || population.isEmpty())
			return;
		Iterator<Solution> pi = population.iterator();
		while (pi.hasNext()) {
			Solution solution = pi.next();
			System.out.print("[");
			for (int objectiveIndex = 0; objectiveIndex < numberOfObjectives; objectiveIndex++) {
				double x = solution.getObjective(objectiveIndex);
				System.out.print(String.format(Locale.ROOT, "%.3f", x));
				if (objectiveIndex < numberOfObjectives - 1)
					System.out.print(" ");
			}
			System.out.print("]");
			if (pi.hasNext()) {
				System.out.print("\t");
			}
		}
		System.out.println();
	}
}
