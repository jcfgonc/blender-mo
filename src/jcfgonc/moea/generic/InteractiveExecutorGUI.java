package jcfgonc.moea.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
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
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

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
	private JLabel epochLabel;
	private int numberOfVariables;
	private int numberOfObjectives;
	private int numberOfConstraints;
	private JLabel variablesLabel;
	private JLabel objectivesLabel;
	private JLabel constraintsLabel;
	private JLabel epochStatus;
	private JLabel variablesStatus;
	private JLabel objectivesStatus;
	private JLabel constraintsStatus;
	private JLabel populationSizeLabel;
	private JLabel populationSizeStatus;
	private Properties algorithmProperties;
	private ArrayList<XYSeries> ndsSeries;
	private JLabel ndsSizeLabel;
	private JLabel ndsSizeStatus;
	private JPanel timeoutPanel;
	private int numberNDSGraphs;
	private Problem problem;
	private JPanel configPanel;
	private JSpinner spinner;
	private JLabel lblNewLabel;
	private JButton printNDS_button;
	private NondominatedPopulation nonDominatedSet;
	private JLabel algorithmLabel;
	private JLabel algorithmStatus;
	private JButton debugButton;
	private JLabel runStatus;
	private JLabel runLabel;
	private JButton nextRunButton;

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
		setTitle("Blender 2.0 - Multiple Objective Optimization");
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

		algorithmLabel = new JLabel("Algorithm: ");
		algorithmLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(algorithmLabel);

		algorithmStatus = new JLabel("");
		algorithmStatus.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(algorithmStatus);

		variablesLabel = new JLabel("Variables: ");
		variablesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(variablesLabel);

		variablesStatus = new JLabel("");
		variablesStatus.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(variablesStatus);

		objectivesLabel = new JLabel("Objectives: ");
		objectivesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(objectivesLabel);

		objectivesStatus = new JLabel("");
		objectivesStatus.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(objectivesStatus);

		constraintsLabel = new JLabel("Constraints: ");
		constraintsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(constraintsLabel);

		constraintsStatus = new JLabel("");
		constraintsStatus.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(constraintsStatus);

		populationSizeLabel = new JLabel("Population Size: ");
		populationSizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(populationSizeLabel);

		populationSizeStatus = new JLabel("");
		populationSizeStatus.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(populationSizeStatus);

		epochLabel = new JLabel("Epoch: ");
		epochLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(epochLabel);

		epochStatus = new JLabel("");
		epochStatus.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(epochStatus);

		runLabel = new JLabel("Run: ");
		runLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(runLabel);

		runStatus = new JLabel("");
		runStatus.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(runStatus);

		ndsSizeLabel = new JLabel("Non-Dominated Set Size: ");
		ndsSizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(ndsSizeLabel);

		ndsSizeStatus = new JLabel("");
		ndsSizeStatus.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(ndsSizeStatus);

		configPanel = new JPanel();
		configPanel.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		settingsPanel.add(configPanel);
		configPanel.setLayout(new GridLayout(0, 1, 0, 0));

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

		nextRunButton = new JButton("Next Run");
		nextRunButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				skipCurrentRun();
			}
		});
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		nextRunButton.setToolTipText("stops the current moea run and starts the next.");
		buttonsPanel.add(nextRunButton);
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

		printNDS_button = new JButton("Print Non Dominated Set");
		printNDS_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printNonDominatedSet();
			}
		});

		debugButton = new JButton("debug");
		debugButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				interactiveExecutor.debug(this);
			}
		});
		debugButton.setToolTipText("does some useful debug thing only I know");
		buttonsPanel.add(debugButton);
		buttonsPanel.add(printNDS_button);

		addComponentListener(new ComponentAdapter() { // window resize event
			@Override
			public void componentResized(ComponentEvent e) {
				windowResized(e);
			}
		});
	}

	private void skipCurrentRun() {
		interactiveExecutor.skipCurrentRun();
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

	@SuppressWarnings("unused")
	private int proportionOfInt(int value, double proportion) {
		double newval = (double) value * proportion;
		return (int) newval;
	}

	/**
	 * contains the rest of the stuff which cannot be initialized in the initialize function (because of the windowbuilder IDE)
	 */
	public void initializeTheRest() {
		variablesStatus.setText(Integer.toString(numberOfVariables));
		objectivesStatus.setText(Integer.toString(numberOfObjectives));
		constraintsStatus.setText(Integer.toString(numberOfConstraints));
		populationSizeStatus.setText(algorithmProperties.getProperty("populationSize"));
		algorithmStatus.setText(interactiveExecutor.getAlgorithmName());

		numberNDSGraphs = (int) Math.ceil((double) numberOfObjectives / 2); // they will be plotted in pairs of objectives

		// if too many objectives put the graphs side by side, otherwise stack them vertically
		if (numberOfObjectives > 4) {
			ndsPanel.setLayout(new GridLayout(1, 0, 0, 0));
		} else {
			ndsPanel.setLayout(new GridLayout(0, 1, 0, 0));
		}

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

//			// color
			XYPlot plot = chart.getXYPlot();
			XYItemRenderer renderer = plot.getRenderer();
			renderer.setSeriesPaint(0, Color.RED);
			Shape shape = new Ellipse2D.Double(-2.5, -2.5, 5, 5);
			renderer.setSeriesShape(0, shape);
			// fill shapes
//			XYStepRenderer rend = (XYStepRenderer) renderer;
//			rend.setShapesFilled(true);

			ChartPanel chartPanel = new ChartPanel(chart, false);
			ndsPanel.add(chartPanel);
		}
//		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//		int width = gd.getDisplayMode().getWidth();
//		int height = gd.getDisplayMode().getHeight();   
//		this.setSize(proportionOfInt(width, 0.333), proportionOfInt(height, 0.333));
//		this.pack();

		// limit jframe size according to windows' high dpi scaling
//		int w = (int) (getPreferredSize().getWidth() * OSTools.getScreenScale());
//		int h = (int) (getPreferredSize().getHeight() * OSTools.getScreenScale());
//		setMinimumSize(new Dimension(w, h));
//		this.setLocationRelativeTo(null); // center jframe

		setMinimumSize(new Dimension(1931, 525));
		setLocation(-6, 0);
		this.pack();

	}

	public void updateStatus(NondominatedPopulation nds, int generation, int run) {
		this.nonDominatedSet = nds;

		epochStatus.setText(Integer.toString(generation));
		runStatus.setText(Integer.toString(run));

		if (nonDominatedSet != null) {
			ndsSizeStatus.setText(Integer.toString(nonDominatedSet.size()));

			// dumb jfreechart
			// draw its stuff in a separate thread and *WAIT* for its completition
			// (because and can not draw new stuff while prior is still been rendered)
			Runnable updater = new Runnable() {
				public void run() {
					// draw NDS/solutions charts
					updateNDSGraphs();
				}
			};
			try {
				SwingUtilities.invokeAndWait(updater);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateNDSGraphs() {
		// update the non-dominated sets' graphs

		int objectiveIndex = 0;
		// iterate the scatter plots (each can hold two objectives)
		for (XYSeries graph : ndsSeries) {
			// empty data series
			graph.clear();
			// iterate the solutions
			for (Solution solution : nonDominatedSet) {
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
//				if (reverseGraphsHorizontally) {
//					x = -x;
//				}
//				if (reverseGraphsVertically) {
//					y = -y;
//				}
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

	private void printNonDominatedSet() {
		if (nonDominatedSet == null || nonDominatedSet.isEmpty())
			return;
		Iterator<Solution> pi = nonDominatedSet.iterator();
		while (pi.hasNext()) {
			Solution solution = pi.next();
			for (int objectiveIndex = 0; objectiveIndex < numberOfObjectives; objectiveIndex++) {
				double x = solution.getObjective(objectiveIndex);
				System.out.print(x);
				if (objectiveIndex < numberOfObjectives - 1)
					System.out.print("\t");
			}
			if (pi.hasNext()) {
				System.out.println();
			}
		}
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
}
