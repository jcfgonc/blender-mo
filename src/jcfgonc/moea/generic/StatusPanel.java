package jcfgonc.moea.generic;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

public class StatusPanel extends JPanel {
	private static final long serialVersionUID = -3946006228924064009L;
	private JLabel algorithmStatus;
	private JLabel variablesStatus;
	private JLabel objectivesStatus;
	private JLabel constraintsStatus;
	private JLabel populationSizeStatus;
	private JLabel epochStatus;
	private JLabel maxRunsStatus;
	private JLabel runStatus;

	public JLabel getMaxEpochsStatus() {
		return maxEpochsStatus;
	}

	private JLabel ndsSizeStatus;
	private JLabel maxEpochsStatus;

	public StatusPanel() {

		setLayout(new GridLayout(0, 2, 0, 0));
		setBorder(new TitledBorder(null, "Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JLabel algorithmLabel = new JLabel("Algorithm: ");
		algorithmLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(algorithmLabel);

		algorithmStatus = new JLabel("");
		algorithmStatus.setHorizontalAlignment(SwingConstants.LEFT);
		add(algorithmStatus);

		JLabel variablesLabel = new JLabel("Variables: ");
		variablesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(variablesLabel);

		variablesStatus = new JLabel("");
		variablesStatus.setHorizontalAlignment(SwingConstants.LEFT);
		add(variablesStatus);

		JLabel objectivesLabel = new JLabel("Objectives: ");
		objectivesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(objectivesLabel);

		objectivesStatus = new JLabel("");
		objectivesStatus.setHorizontalAlignment(SwingConstants.LEFT);
		add(objectivesStatus);

		JLabel constraintsLabel = new JLabel("Constraints: ");
		constraintsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(constraintsLabel);

		constraintsStatus = new JLabel("");
		constraintsStatus.setHorizontalAlignment(SwingConstants.LEFT);
		add(constraintsStatus);

		JLabel populationSizeLabel = new JLabel("Population Size: ");
		populationSizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(populationSizeLabel);

		populationSizeStatus = new JLabel("");
		populationSizeStatus.setHorizontalAlignment(SwingConstants.LEFT);
		add(populationSizeStatus);

		JLabel maxEpochsLabel = new JLabel("Maximum Epochs: ");
		maxEpochsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(maxEpochsLabel);

		maxEpochsStatus = new JLabel("");
		maxEpochsStatus.setHorizontalAlignment(SwingConstants.LEFT);
		add(maxEpochsStatus);

		JLabel epochLabel = new JLabel("Epoch: ");
		epochLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(epochLabel);

		epochStatus = new JLabel("");
		epochStatus.setHorizontalAlignment(SwingConstants.LEFT);
		add(epochStatus);

		JLabel maxRunsLabel = new JLabel("Maximum Runs: ");
		maxRunsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(maxRunsLabel);

		maxRunsStatus = new JLabel("");
		maxRunsStatus.setHorizontalAlignment(SwingConstants.LEFT);
		add(maxRunsStatus);

		JLabel runLabel = new JLabel("Run: ");
		runLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(runLabel);

		runStatus = new JLabel("");
		runStatus.setHorizontalAlignment(SwingConstants.LEFT);
		add(runStatus);

		JLabel ndsSizeLabel = new JLabel("Non-Dominated Set Size: ");
		ndsSizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(ndsSizeLabel);

		ndsSizeStatus = new JLabel("");
		ndsSizeStatus.setHorizontalAlignment(SwingConstants.LEFT);
		add(ndsSizeStatus);
	}

	public JLabel getAlgorithmStatus() {
		return algorithmStatus;
	}

	public JLabel getConstraintsStatus() {
		return constraintsStatus;
	}

	public JLabel getEpochStatus() {
		return epochStatus;
	}

	public JLabel getMaxRunsStatus() {
		return maxRunsStatus;
	}

	public JLabel getNdsSizeStatus() {
		return ndsSizeStatus;
	}

	public JLabel getObjectivesStatus() {
		return objectivesStatus;
	}

	public JLabel getPopulationSizeStatus() {
		return populationSizeStatus;
	}

	public JLabel getRunStatus() {
		return runStatus;
	}

	public JLabel getVariablesStatus() {
		return variablesStatus;
	}

}
