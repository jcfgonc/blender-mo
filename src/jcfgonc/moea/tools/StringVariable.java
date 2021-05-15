package jcfgonc.moea.tools;

import java.util.ArrayList;

import org.moeaframework.core.Variable;

/**
 * @author "Joao Goncalves: jcfgonc@gmail.com"
 *
 */
public class StringVariable implements Variable {

	private static final long serialVersionUID = 1325766775637018352L;
	final private String text;
	final private ArrayList<String> fields;

	@Override
	public String toString() {
		return text;
	}

	StringVariable(String text) {
		this.text = text;
		this.fields = new ArrayList<String>();
	}

	@Override
	public Variable copy() {
		return null;
	}

	@Override
	public void randomize() {
	}

	public String getText() {
		return text;
	}

	public ArrayList<String> getFields() {
		return fields;
	}

	public void addField(String fieldData) {
		fields.add(fieldData);
	}

	public int getNumberFields() {
		return fields.size();
	}

	public String getField(int i) {
		return fields.get(i);
	}
}