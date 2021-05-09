package jcfgonc.moea.tools;

import java.io.IOException;
import java.util.ArrayList;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;

import structures.CSVReader;

public class DuplicateResultsRemoverMO {

	@SuppressWarnings("serial")
	class StringVariable implements Variable {

		final private String text;
		final private int group;

		StringVariable(String text, int group) {
			this.text = text;
			this.group = group;
		}

		@Override
		public Variable copy() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void randomize() {
			// TODO Auto-generated method stub

		}

		public String getText() {
			return text;
		}

		public int getGroup() {
			return group;
		}
	}

	public static void main(String[] args) throws IOException {
		String folder = "..\\BlenderMO\\results";
		String datafile = "C:\\Desktop\\github\\BlenderMO\\results\\dataset.tsv";
		CSVReader reader = new CSVReader("\t", datafile, true);

		// --- BLENDERMO results HEADER
		// d:class f:mean of within-blend semantic similarity f:mean importance of vital relations f:input spaces balance f:mapping mix f:relation
		// similarity d:graph's vertices d:graph's edges f:novelty g:blend space

		NondominatedPopulation pop = new NondominatedPopulation();
		for (ArrayList<String> row : reader.getRows()) {
			System.out.println(row);
			Solution solution = new Solution(1, 5);
			solution.setVariable(0, new StringVariable(datafile));
			pop.add(solution);
		}

//		DoubleCSVReader dr = new DoubleCSVReader("\t", datafile, true);
//		for (int i = 0; i < dr.getNumberOfRows(); i++) {
//			System.out.println(dr.getRow(i));
//		}
	}
}
