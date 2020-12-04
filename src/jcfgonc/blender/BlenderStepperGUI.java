package jcfgonc.blender;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.DefaultView;

import graph.StringGraph;
import utils.OSTools;
import visual.GraphData;

@SuppressWarnings({ "unused", "serial" })
public class BlenderStepperGUI extends JFrame {
	private static final int FONT_SIZE_MINIMUM = 8;
	private static final int FONT_SIZE_DEFAULT = 24;
	private static final int FONT_SIZE_MAXIMUM = 48;
	private static final int NODE_SIZE_MINIMUM = 0;
	private static final int NODE_SIZE_DEFAULT = 16;
	private static final int NODE_SIZE_MAXIMUM = 100;
	private static final int GRAPHS_PER_COLUMN_MINIMUM = 1;
	private static final int GRAPHS_PER_COLUMN_DEFAULT = 4;
	private static final int GRAPHS_PER_COLUMN_MAXIMUM = 10;

	private JPanel contentPane;
	private Semaphore stepSem;
	private JPanel graphPanel;
	private JScrollPane scrollPane;
	private List<GraphData> graphs;
	private int graphFontSize = FONT_SIZE_DEFAULT;
	private int graphNodeSize = NODE_SIZE_DEFAULT;
	private int graphsPerColumn = GRAPHS_PER_COLUMN_DEFAULT;
	private int graphSize;

	public BlenderStepperGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane); // TODO remove this and check if still works

		graphPanel = new JPanel();
		scrollPane = new JScrollPane(graphPanel);
		contentPane.add(scrollPane);
		scrollPane.setViewportBorder(null);
		scrollPane.setDoubleBuffered(true);
		scrollPane.setBorder(null);
		scrollPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.getVerticalScrollBar().setUnitIncrement(32);
		graphPanel.setBorder(null);
		graphPanel.setLayout(new GridLayout(1, 0, 0, 0));
		scrollPane.getViewport().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// updateGraphVisibility(e);
			}
		});

		addComponentListener(new ComponentAdapter() { // window resize event
			@Override
			public void componentResized(ComponentEvent e) {
				windowResized();
			}
		});
	}

	private void windowResized() {
		layoutGraphPanel();
	}

	private void createAndShowGUI(List<GraphData> graphs) {
		double w = 640 * OSTools.getScreenScale();
		double h = 480 * OSTools.getScreenScale();
		setSize(new Dimension((int) w, (int) h));

		this.graphs = graphs;
		addGraphsToPanel();
		layoutGraphPanel();
		updateFontsSize();
		updateNodesSize();

		// center window
		setLocationRelativeTo(null);
		registerKeyboard();
		setVisible(true);
	}

	/**
	 * configures the layout of the panel showing the graphs
	 */
	private void layoutGraphPanel() {
		if (graphs.isEmpty())
			return;
		if (graphs.size() < graphsPerColumn) {
			graphsPerColumn = graphs.size();
		}
		int panelWidth = scrollPane.getViewport().getWidth();
		this.graphSize = panelWidth / graphsPerColumn - 2;
		updateGraphsSize();
		GridLayout layout = (GridLayout) graphPanel.getLayout();
		layout.setColumns(graphsPerColumn);
		layout.setRows(0);
		// numColumnsLabel.setText(Integer.toString(graphsPerColumn)); //label showing number of graphs per column (not present in the gui)
		graphPanel.revalidate();
		graphPanel.repaint();
		// numGraphsSlider.setValue(graphsPerColumn);
	}

	private void addGraphsToPanel() {
		if (graphs.isEmpty())
			return;

		for (GraphData gd : graphs) {
			DefaultView dv = gd.getDefaultView();
			graphPanel.add(dv);
			gd.getViewer().enableAutoLayout();
		}

		updateGraphsSize();
	}

	/**
	 * updates the size of the graph gui components
	 */
	private void updateGraphsSize() {
		for (GraphData gd : graphs) {
			DefaultView view = gd.getDefaultView();
			Dimension size = new Dimension(graphSize, graphSize);
			view.setPreferredSize(size);
		}
	}

	private void registerKeyboard() {
		contentPane.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "space pressed");
		contentPane.getInputMap().put(KeyStroke.getKeyStroke("A"), "a pressed");
		contentPane.getInputMap().put(KeyStroke.getKeyStroke("S"), "s pressed");
		contentPane.getInputMap().put(KeyStroke.getKeyStroke("C"), "c pressed");

		contentPane.getActionMap().put("space pressed", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(e.toString());
				stepSem.release();
			}
		});
		contentPane.getActionMap().put("a pressed", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(e.toString());
			}
		});
		contentPane.getActionMap().put("s pressed", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(e.toString());
			}
		});
		contentPane.getActionMap().put("c pressed", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(e.toString());
			}
		});
	}

	public void setup(List<GraphData> graphs, Semaphore stepSem) {
		this.stepSem = stepSem;

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					createAndShowGUI(graphs);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	/**
	 * checks for differences between each internal GraphData's semantic graph and the given graphs
	 * 
	 * @param newGraphs
	 */
	public void updateBlendGraph(List<StringGraph> newGraphs) {
		if(newGraphs.size() != graphs.size()) {
			throw new RuntimeException();
		}

		for (int i = 0; i < graphs.size(); i++) {
			updateBlendGraph(i, newGraphs.get(i));
		}
	}

	public void updateBlendGraph(int i, StringGraph newStringGraph) {
		GraphData gd = graphs.get(i);
		gd.updateGraph(newStringGraph);
	}

	private void updateFontsSize() {
		graphs.parallelStream().forEach(gd -> {
			MultiGraph graph = gd.getMultiGraph();
			String style = String.format("edge { text-size: %d; } node { text-size: %d; }", graphFontSize, graphFontSize);
			graph.addAttribute("ui.stylesheet", style);
		});
	}

	private void updateNodesSize() {
		graphs.parallelStream().forEach(gd -> {
			MultiGraph graph = gd.getMultiGraph();
			String style;
			if (graphNodeSize == 0) {
				style = String.format("node { stroke-mode: none; }");
			} else {
				style = String.format("node { stroke-mode: plain; size: %dpx; }", graphNodeSize, graphNodeSize);
			}
			graph.addAttribute("ui.stylesheet", style);
		});
	}

	public void shakeGraphs() {
		graphs.stream().forEach(gd -> {
			gd.getLayout().shake();
		});
	}

}
