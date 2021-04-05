package jcfgonc.moea.generic;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class SettingsPanel extends JPanel {
	private static final long serialVersionUID = -321394905164631735L;

	public SettingsPanel() {
		super();

		setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new GridLayout(0, 1, 0, 0));

	}
}
