package jcfgonc.moea.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

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
	private JPanel rightPanel;
	private InteractiveExecutor interactiveExecutor;
	private int numberOfVariables;
	private int numberOfObjectives;
	private int numberOfConstraints;
	private Properties algorithmProperties;
	private ArrayList<XYSeries> ndsSeries;
	private int numberNDSGraphs;
	private Problem problem;
	private NondominatedPopulation nonDominatedSet;
	private StatusPanel statusPanel;
	private OptimisationControlPanel optimisationControlPanel;
	private JPanel fillPanel;

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
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		horizontalPane = new JSplitPane();
		horizontalPane.setEnabled(false);
		horizontalPane.setContinuousLayout(true);
		horizontalPane.setBorder(null);
		contentPane.add(horizontalPane);

		ndsPanel = new JPanel();
		ndsPanel.setBorder(null);
		horizontalPane.setLeftComponent(ndsPanel);
		ndsPanel.setLayout(new GridLayout(0, 1, 0, 0));

		rightPanel = new JPanel();
		rightPanel.setBorder(null);
		horizontalPane.setRightComponent(rightPanel);
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

		statusPanel = new StatusPanel();
		rightPanel.add(statusPanel);

		optimisationControlPanel = new OptimisationControlPanel();
		rightPanel.add(optimisationControlPanel);

		fillPanel = new JPanel();
		fillPanel.setBorder(null);
		rightPanel.add(fillPanel);
		fillPanel.setLayout(new BorderLayout(0, 0));

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
		statusPanel.getVariablesStatus().setText(Integer.toString(numberOfVariables));
		statusPanel.getObjectivesStatus().setText(Integer.toString(numberOfObjectives));
		statusPanel.getConstraintsStatus().setText(Integer.toString(numberOfConstraints));
		statusPanel.getPopulationSizeStatus().setText(algorithmProperties.getProperty("populationSize"));
		statusPanel.getAlgorithmStatus().setText(interactiveExecutor.getAlgorithmName());
		statusPanel.getMaxEpochsStatus().setText(Integer.toString(interactiveExecutor.getMaxEpochs()));
		statusPanel.getMaxRunsStatus().setText(Integer.toString(interactiveExecutor.getMaxRuns()));

		optimisationControlPanel.setInteractiveExecutorGUI(this);

		numberNDSGraphs = (int) Math.ceil((double) numberOfObjectives / 2); // they will be plotted in pairs of objectives

		// if too many objectives put the graphs side by side, otherwise stack them vertically
		if (numberOfObjectives > 2) {
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

			JFreeChart chart = ChartFactory.createScatterPlot(null, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, false, false, false);

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

//		this.setLocationRelativeTo(null); // center jframe

		setPreferredSize(new Dimension(1931, 525));
		setLocation(-6, 0);
		this.pack();

	}

	/**
	 * Update the GUI. If the given NondominatedPopulation is null or empty this function just updates the epoch and run JLabels.
	 * 
	 * @param nds
	 * @param epoch
	 * @param run
	 */
	public void updateStatus(NondominatedPopulation nds, int epoch, int run) {
		this.nonDominatedSet = nds;

		statusPanel.getEpochStatus().setText(Integer.toString(epoch));
		statusPanel.getRunStatus().setText(Integer.toString(run));

		if (nds != null && !nds.isEmpty()) {
			statusPanel.getNdsSizeStatus().setText(Integer.toString(nds.size()));

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
		horizontalPane.setDividerLocation(horizontalPane.getWidth() - rightPanel.getMinimumSize().width);
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

	public void skipCurrentRun() {
		interactiveExecutor.skipCurrentRun();
	}

	public void debug() {
		System.out.println(this.getLocation());
		System.out.println(this.getSize());
	}
}
