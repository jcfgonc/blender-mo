package jcfgonc.moea.generic;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class OptimisationControlPanel extends JPanel {
	private static final long serialVersionUID = 2778229888241955365L;
	private InteractiveExecutorGUI gui;

	public OptimisationControlPanel() {

		setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),

				"Optimization Control", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton nextRunButton = new JButton("Next Run");
		nextRunButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gui.skipCurrentRun();
			}
		});
		nextRunButton.setToolTipText("stops the current moea run and starts the next.");
		add(nextRunButton);

		JButton stopButton = new JButton("Stop Optimization");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gui.stopOptimization();
			}
		});
		stopButton.setToolTipText("Waits for the current epoch to complete and returns the best results so far.");
		stopButton.setBorder(UIManager.getBorder("Button.border"));
		stopButton.setAlignmentX(0.5f);
		add(stopButton);

		JButton abortButton = new JButton("Abort Optimization");
		abortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// System.out.println((double) (horizontalPane.getDividerLocation()) / (horizontalPane.getWidth() - horizontalPane.getDividerSize()));
				gui.abortOptimization();
			}
		});
		abortButton.setToolTipText("Aborts the optimization by discarding the current epoch's results and returns the best results so far.");
		abortButton.setBorder(UIManager.getBorder("Button.border"));
		abortButton.setAlignmentX(0.5f);
		add(abortButton);

		JButton debugButton = new JButton("debug");
		debugButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gui.debug();
			}
		});
		debugButton.setToolTipText("does some useful debug thing only I know");
		add(debugButton);

		JButton printNDS_button = new JButton("Print Non Dominated Set");
		printNDS_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gui.printNonDominatedSet();
			}
		});
		add(printNDS_button);
	}

	public void setInteractiveExecutorGUI(InteractiveExecutorGUI gui) {
		this.gui = gui;
	}

}
