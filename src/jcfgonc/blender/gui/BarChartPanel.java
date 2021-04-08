package jcfgonc.blender.gui;

import java.awt.GridLayout;
import java.awt.Paint;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class BarChartPanel extends JPanel {

	private static final long serialVersionUID = 6897997626552672853L;
	private DefaultCategoryDataset dataset;
	private String valueAxisLabel;
	private String categoryAxisLabel;
	private String title;
	private Paint paint;

	public BarChartPanel(String title, String categoryAxisLabel, String valueAxisLabel, Paint paint) {
		this.title = title;
		this.categoryAxisLabel = categoryAxisLabel;
		this.valueAxisLabel = valueAxisLabel;
		this.paint = paint;
		// this is important, otherwise the panel will overflow its bounds
		setBorder(null);
		setLayout(new GridLayout(1, 0, 0, 0));
	}

	public void initialize() {
		dataset = new DefaultCategoryDataset();
		JFreeChart barChart = ChartFactory.createBarChart(title, categoryAxisLabel, valueAxisLabel, dataset, PlotOrientation.VERTICAL, false, false,
				false);
		ChartPanel chartPanel = new ChartPanel(barChart);
		CategoryPlot plot = barChart.getCategoryPlot();
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		// this is to change the type of bar
//		StandardBarPainter painter = new StandardBarPainter();
//		renderer.setBarPainter(painter);
		// disable bar shadows
		renderer.setShadowVisible(false);
		renderer.setSeriesPaint(0, paint);
		renderer.setItemMargin(0.0); 
		plot.setBackgroundAlpha(1);
		CategoryAxis domainAxis = plot.getDomainAxis();
		// hide domain axis/labels
		domainAxis.setAxisLineVisible(false);
		domainAxis.setVisible(false);
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);
		domainAxis.setCategoryMargin(0.0);
		 // ValueAxis rangeAxis = plot.getRangeAxis();
		add(chartPanel);
	}

	public void addSample(double x, double y, String category) {
		// second and third arguments must be constant
		dataset.addValue(y, category, Double.toString(x));
	}

	public void addSample(int x, double y, String category) {
		// second and third arguments must be constant
		dataset.addValue(y, category, Integer.toString(x));
	}

	public void clearData() {
		dataset.clear();
	}

}
