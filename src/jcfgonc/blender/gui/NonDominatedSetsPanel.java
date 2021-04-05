package jcfgonc.blender.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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

import jcfgonc.moea.generic.ProblemDescription;

public class NonDominatedSetsPanel extends JPanel {

	private static final long serialVersionUID = -3640671737661415056L;
	private int numberOfObjectives;
	private ArrayList<XYSeries> ndsSeries;
	private NondominatedPopulation nonDominatedSet;

	public NonDominatedSetsPanel() {
		setBorder(null);
		setLayout(new GridLayout(1, 0, 0, 0));
	}

	public void initialize(Problem problem) {
		numberOfObjectives = problem.getNumberOfObjectives();
		
		// if too many objectives put the graphs side by side, otherwise stack them vertically
		if (numberOfObjectives > 2) {
			setLayout(new GridLayout(1, 0, 0, 0));
		} else {
			setLayout(new GridLayout(0, 1, 0, 0));
		}

		int numberNDSGraphs = (int) Math.ceil((double) numberOfObjectives / 2); // they will be plotted in pairs of objectives

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

			ChartPanel chartPanel = new ChartPanel(chart, true);
			add(chartPanel);
		}
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

	/**
	 * Called by the main GUI class, who knows everything.
	 * 
	 * @param nds
	 */
	public void updateGraphs(NondominatedPopulation nds) {
		this.nonDominatedSet = nds;
		// dumb jfreechart
		// draw its stuff in a separate thread and *WAIT* for its completion
		// (because and can not draw new stuff while prior is still been rendered)
		Runnable updater = new Runnable() {
			public void run() {
				// draw NDS/solutions charts
				refillXYSeries();
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

	/**
	 * Used by the async thread to run JFreeChart's rendering code.
	 */
	private void refillXYSeries() {
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
				graph.add(x, y);
			}

			objectiveIndex += 2;
		}
	}
}
