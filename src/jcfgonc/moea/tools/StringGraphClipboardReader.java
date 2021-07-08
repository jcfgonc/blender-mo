package jcfgonc.moea.tools;

import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import graph.GraphReadWrite;
import graph.StringGraph;
import visual.VisualGraph;

public class StringGraphClipboardReader {

	public static void main(String[] args) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		System.setProperty("org.graphstream.ui", "org.graphstream.ui.swingViewer.util.SwingDisplay");
		System.setProperty("org.graphstream.ui", "swing");

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					createAndShowGUI();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
			}
		});
	}

	protected static void createAndShowGUI()
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		VisualGraph vg = new VisualGraph(0);

		JFrame mainFrame = new JFrame("StringGraph Clipboard Reader");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setPreferredSize(new Dimension(320, 240));
		mainFrame.pack();
		mainFrame.setVisible(true);
		mainFrame.addMouseWheelListener(new MouseWheelListener() {
			double magnification = 1.0;
			final double magDelta = 1.03333333;

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// if (e.isControlDown()) {
		//		System.out.println(e);
				if (e.getWheelRotation() < 0) {
					magnification = magnification / magDelta;
					vg.changeMagnification(magnification);
					// System.out.println("mouse wheel Up");
				} else {
					magnification = magnification * magDelta;
					vg.changeMagnification(magnification);
					// System.out.println("mouse wheel Down");
				}
				// } else { // pass event to parent
				// Container parent = mainFrame.getParent();
				// if (parent != null) {
				// parent.dispatchEvent(e);
				// }
				// }
			}
		});

		mainFrame.add(vg.getDefaultView());

		StringClipBoardListener cl = new StringClipBoardListener(clipboardText -> {
			// System.out.println(clipboardText);
			try {
				StringGraph graph = GraphReadWrite.readCSVFromString(clipboardText);
				if (!graph.isEmpty()) {
					vg.refreshGraph(graph);
//					System.out.println(graph);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		cl.start();
	}

}
