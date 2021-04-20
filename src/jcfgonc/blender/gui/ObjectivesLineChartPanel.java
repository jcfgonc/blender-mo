package jcfgonc.blender.gui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.moeaframework.core.Problem;

import jcfgonc.moea.generic.ProblemDescription;

/**
 * This class handles the drawing of multiple lines, each associated to an objective.
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class ObjectivesLineChartPanel extends JPanel {

	private static final long serialVersionUID = 6897997626552672853L;
	final private DefaultCategoryDataset dataset;
	final private String valueAxisLabel;
	final private String categoryAxisLabel;
	final private String title;
//	private CategoryPlot plot;
//	private Paint paint;
	final private Problem problem;
	final private List<String> nameObjectives;
	final private int numberObjectives;

	public ObjectivesLineChartPanel(String title, String categoryAxisLabel, String valueAxisLabel, Problem problem) {
		this.title = title;
		this.categoryAxisLabel = categoryAxisLabel;
		this.valueAxisLabel = valueAxisLabel;
		this.problem = problem;
		this.numberObjectives = problem.getNumberOfObjectives();
		this.nameObjectives = new ArrayList<String>(numberObjectives);
		this.dataset = new DefaultCategoryDataset();
//		this.paint = paint;
		// this is important, otherwise the panel will overflow its bounds
		setBorder(null);
		setLayout(new GridLayout(1, 0, 0, 0));
	}

	public void initialize() {
		JFreeChart chart = ChartFactory.createLineChart(title, categoryAxisLabel, valueAxisLabel, dataset, PlotOrientation.VERTICAL, true, false,
				false);
		chart.setRenderingHints(GUI_Utils.createDefaultRenderingHints());
		ChartPanel chartPanel = new ChartPanel(chart);
		CategoryPlot plot = chart.getCategoryPlot();
//		CategoryItemRenderer renderer = plot.getRenderer();
		// this is to change the type of bar
//		StandardBarPainter painter = new StandardBarPainter();
//		renderer.setBarPainter(painter);
		// disable bar shadows
//		renderer.setShadowVisible(false);
//		renderer.setSeriesPaint(0, paint);
//		plot.setBackgroundAlpha(1);
		CategoryAxis domainAxis = plot.getDomainAxis();
		// hide domain axis/labels
		domainAxis.setAxisLineVisible(false);
		domainAxis.setVisible(false);
		// ValueAxis rangeAxis = plot.getRangeAxis();

		for (int i = 0; i < numberObjectives; i++) {
			String category;
			if (problem instanceof ProblemDescription) {
				ProblemDescription pd = (ProblemDescription) problem;
				category = pd.getObjectiveDescription(i);
			} else {
				category = String.format("Objective %d", i);
			}
			nameObjectives.add(category);
		}

		add(chartPanel);
	}

	public void addValue(double y, int x, String category) {
		// second and third arguments must be constant
		dataset.addValue(y, category, Integer.toString(x));
	}

	public void addValue(double[] ys, int x) {
		for (int i = 0; i < numberObjectives; i++) {
			double y = ys[i];
			String objectiveName = nameObjectives.get(i);
			addValue(y, x, objectiveName);
		}
	}

	public void clearData() {
		dataset.clear();
	}

}
