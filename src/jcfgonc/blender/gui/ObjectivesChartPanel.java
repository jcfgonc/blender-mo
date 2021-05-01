package jcfgonc.blender.gui;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import jcfgonc.moea.generic.ProblemDescription;

/**
 * This class handles the drawing of multiple lines, each associated to an objective.
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class ObjectivesChartPanel extends JPanel {

	private static final long serialVersionUID = 6897997626552672853L;
	final private DefaultBoxAndWhiskerCategoryDataset dataset;
	final private String valueAxisLabel;
	final private String categoryAxisLabel;
	final private String title;
//	private CategoryPlot plot;
//	private Paint paint;
	final private Problem problem;
	final private int numberOfObjectives;
	final private ArrayList<String> nameObjectives;

	public ObjectivesChartPanel(String title, String categoryAxisLabel, String valueAxisLabel, Problem problem) {
		this.title = title;
		this.categoryAxisLabel = categoryAxisLabel;
		this.valueAxisLabel = valueAxisLabel;
		this.problem = problem;
		this.numberOfObjectives = problem.getNumberOfObjectives();
		this.nameObjectives = new ArrayList<String>(numberOfObjectives);
		this.dataset = new DefaultBoxAndWhiskerCategoryDataset();

		// list of objectives description
		for (int objective_i = 0; objective_i < numberOfObjectives; objective_i++) {
			String category;
			if (problem instanceof ProblemDescription) {
				ProblemDescription pd = (ProblemDescription) problem;
				category = pd.getObjectiveDescription(objective_i);
				category = category.substring(category.indexOf(':') + 1);
			} else {
				category = String.format("Objective %d", objective_i);
			}
			nameObjectives.add(category);
		}

		// this is important, otherwise the panel will overflow its bounds
		setBorder(null);
		setLayout(new GridLayout(1, 0, 0, 0));
	}

	public void initialize() {
		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(title, categoryAxisLabel, valueAxisLabel, dataset, false);
		chart.setRenderingHints(GUI_Utils.createDefaultRenderingHints());
		ChartPanel chartPanel = new ChartPanel(chart);
//		CategoryPlot plot = chart.getCategoryPlot();
//		CategoryItemRenderer renderer = plot.getRenderer();
		// this is to change the type of bar
//		StandardBarPainter painter = new StandardBarPainter();
//		renderer.setBarPainter(painter);
		// disable bar shadows
//		renderer.setShadowVisible(false);
//		renderer.setSeriesPaint(0, paint);
//		plot.setBackgroundAlpha(1);
//		CategoryAxis domainAxis = plot.getDomainAxis();
		// hide domain axis/labels
//		domainAxis.setAxisLineVisible(false);
//		domainAxis.setVisible(false);
		// ValueAxis rangeAxis = plot.getRangeAxis();

		add(chartPanel);
	}

	public void clearData() {
		dataset.clear();
	}

	public void addValues(NondominatedPopulation nds) {
		dataset.setNotify(false);

		int numberOfSolutions = nds.size();
		clearData();
		// create an array per objective to store each solutions' objective
		ArrayList<ArrayList<Double>> boxplotLists = new ArrayList<ArrayList<Double>>(numberOfObjectives);
		for (int objective_i = 0; objective_i < numberOfObjectives; objective_i++) {
			ArrayList<Double> boxplotList = new ArrayList<Double>(numberOfSolutions);
			boxplotLists.add(boxplotList);
		}

		for (int solution_i = 0; solution_i < numberOfSolutions; solution_i++) {
			Solution solution = nds.get(solution_i);
			// for each objective
			for (int objective_i = 0; objective_i < numberOfObjectives; objective_i++) {
				ArrayList<Double> boxplotData = boxplotLists.get(objective_i);
				double objValue = solution.getObjective(objective_i);
				boxplotData.add(Double.valueOf(objValue));
			}
		}

		for (int objective_i = 0; objective_i < numberOfObjectives; objective_i++) {
//			String category;
//			if (problem instanceof ProblemDescription) {
//				ProblemDescription pd = (ProblemDescription) problem;
//				category = pd.getObjectiveDescription(objective_i);
//				category = category.substring(category.indexOf(':') + 1);
//			} else {
//				category = String.format("Objective %d", objective_i);
//			}

			ArrayList<Double> boxplotData = boxplotLists.get(objective_i);
			// dataset.add(boxplotData, category,"0");
			dataset.add(boxplotData, "0", String.format("(%d)", objective_i));
			// dataset.add(boxplotData, "0", String.format("(%d) %s", objective_i, category));
		}
		dataset.setNotify(true);
	}
}
