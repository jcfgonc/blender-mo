package jcfgonc.blender.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jcfgonc.blender.MOEA_Config;

public class SettingsPanel extends JPanel {
	private static final long serialVersionUID = -321394905164631735L;
	private JSpinner numberEpochs;
	private JSpinner numberRuns;
	private JSpinner populationSize;
	private JCheckBox performanceGraphsEnabledCB;
	private JCheckBox screenshotsCB;
	private boolean performanceGraphsEnabled = MOEA_Config.PERFORMANCE_GRAPHS_ENABLED;
	private boolean screenshotsEnabled = MOEA_Config.SCREENSHOTS_ENABLED;

	public SettingsPanel() {
		super();

		setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel panel_0 = new JPanel();
		add(panel_0);

		JLabel label_0 = new JLabel("Performance Graphs Enabled");
		panel_0.add(label_0);

		performanceGraphsEnabledCB = new JCheckBox("", performanceGraphsEnabled);
		performanceGraphsEnabledCB.setToolTipText("If enabled updates the performance graphs to the left.");
		performanceGraphsEnabledCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graphsEnabledClickEvent();
			}
		});
		panel_0.add(performanceGraphsEnabledCB);

		JPanel panel_1 = new JPanel();
		add(panel_1);

		JLabel label_1 = new JLabel("Number of Epochs per Run");
		panel_1.add(label_1);

		numberEpochs = new JSpinner();
		numberEpochs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Integer value = (Integer) numberEpochs.getModel().getValue();
				MOEA_Config.MAX_EPOCHS = value.intValue();
			}
		});
		numberEpochs.setModel(new SpinnerNumberModel(512, 20, 16384, 1));
		numberEpochs.setToolTipText("Number of epochs to execute for each run. Takes effect immediately.");
		panel_1.add(numberEpochs);

		JPanel panel_2 = new JPanel();
		add(panel_2);

		JLabel label_2 = new JLabel("Number of Runs");
		panel_2.add(label_2);

		numberRuns = new JSpinner();
		numberRuns.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Integer value = (Integer) numberRuns.getModel().getValue();
				MOEA_Config.MOEA_RUNS = value.intValue();
			}
		});
		numberRuns.setModel(new SpinnerNumberModel(256, 20, 16384, 1));
		numberRuns.setToolTipText("Number of runs (each of n epochs) to execute. Takes effect immediately.");
		panel_2.add(numberRuns);

		JPanel panel_3 = new JPanel();
		add(panel_3);

		JLabel label_3 = new JLabel("Population Size");
		panel_3.add(label_3);

		populationSize = new JSpinner();
		populationSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Integer value = (Integer) populationSize.getModel().getValue();
				MOEA_Config.POPULATION_SIZE = value.intValue();
			}
		});
		populationSize.setModel(new SpinnerNumberModel(256, 20, 16384, 1));
		populationSize.setToolTipText("Size of the population. Takes effect on the next run.");
		panel_3.add(populationSize);

		JPanel panel_4 = new JPanel();
		add(panel_4);

		JLabel label_4 = new JLabel("Take Runtime Screenshots");
		panel_4.add(label_4);

		screenshotsCB = new JCheckBox("", screenshotsEnabled);
		screenshotsCB.setToolTipText("If enabled takes a screenshot of the GUI every epoch.");
		screenshotsCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				screenshotsEnabledClickEvent();
			}
		});
		panel_4.add(screenshotsCB);

	}

	public void graphsEnabledClickEvent() {
		performanceGraphsEnabled = performanceGraphsEnabledCB.isSelected();
	}

	public boolean isPerformanceGraphsEnabled() {
		return performanceGraphsEnabled;
	}

	public boolean isScreenshotsEnabled() {
		return screenshotsEnabled;
	}

	public void screenshotsEnabledClickEvent() {
		screenshotsEnabled = screenshotsCB.isSelected();
	}

	public void setNumberEpochs(int v) {
		numberEpochs.setValue(v);
	}

	public void setNumberRuns(int v) {
		numberRuns.setValue(v);
	}

	public void setPopulationSize(int v) {
		populationSize.setValue(v);
	}
}
