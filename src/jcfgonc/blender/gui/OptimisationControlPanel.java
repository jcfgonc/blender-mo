package jcfgonc.blender.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class OptimisationControlPanel extends JPanel {
	private static final long serialVersionUID = 2778229888241955365L;
	private InteractiveExecutorGUI gui;
	private JButton nextRunButton;
	private JButton stopButton;
	private AbstractButton abortButton;
	private JButton debugButton;
	private JButton printNDS_button;
	private JPanel panel0;
	private JPanel panel1;
	private JPanel panel2;

	public OptimisationControlPanel() {

		setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),

				"Optimization Control", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		panel0 = new JPanel();
		add(panel0);

		panel1 = new JPanel();
		add(panel1);

		panel2 = new JPanel();
		add(panel2);

		printNDS_button = new JButton("Print Non Dominated Set");
		printNDS_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gui.printNonDominatedSet();
			}
		});
		panel0.add(printNDS_button);

		nextRunButton = new JButton("Next Run");
		nextRunButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gui.skipCurrentRun();
			}
		});
		nextRunButton.setToolTipText("stops the current moea run and starts the next.");
		panel0.add(nextRunButton);

		stopButton = new JButton("Stop Optimization");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gui.stopOptimization();
			}
		});
		stopButton.setToolTipText("Waits for the current epoch to complete and returns the best results so far.");
		panel1.add(stopButton);

		abortButton = new JButton("Abort Optimization");
		abortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// System.out.println((double) (horizontalPane.getDividerLocation()) / (horizontalPane.getWidth() - horizontalPane.getDividerSize()));
				gui.abortOptimization();
			}
		});
		abortButton.setToolTipText("Aborts the optimization by discarding the current epoch's results and returns the best results so far.");
		panel1.add(abortButton);

		debugButton = new JButton("debug");
		debugButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				debug();
			}
		});
		debugButton.setToolTipText("does some useful debug thing only I know");
		panel2.add(debugButton);
	}

	public void setInteractiveExecutorGUI(InteractiveExecutorGUI gui) {
		this.gui = gui;
	}

	public void debug() {
		System.out.println(this.getSize());
//		gui.debug();
	}

}
