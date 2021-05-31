package jcfgonc.moea.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import utils.VariousUtils;

/**
 * Removes (multi-objective) dominated solutions from the results file.
 * 
 * @author "Joao Goncalves: jcfgonc@gmail.com"
 *
 */
public class RemoveDominatedResults {

	public static void main(String[] args) throws IOException {
		// --- BLENDERMO results HEADER
		// first columns are the objectives
		// last 4 columns are
		// d:graph's vertices
		// d:graph's edges
		// f:novelty
		// g:blend space
		final int numberNonObjectiveColumns = 4;
		String filename = "using frames.tsv";

		ArrayList<Solution> solutions = new ArrayList<Solution>();
		String header = readResultsFile(solutions, filename, numberNonObjectiveColumns, 0);

		System.out.println("read a total of " + solutions.size() + " solutions");

		// remove dominated solutions
		NondominatedPopulation pop = new NondominatedPopulation();
		for (int i = 0; i < solutions.size(); i++) {
			pop.add(solutions.get(i));
		}

		System.out.printf("%d duplicated solutions\n", (solutions.size() - pop.size()));
		System.out.println(pop.size() + " unique solutions");

		saveResultsFile(pop, VariousUtils.appendSuffixToFilename(filename, "_nondominated"), header, 0);
	}

	private static void saveResultsFile(Iterable<Solution> pop, String datafile, String header, int clazz) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(datafile, StandardCharsets.UTF_8), 1 << 16);
		bw.write(header);
		bw.newLine();
		for (Solution sol : pop) {
			StringVariable variable = (StringVariable) sol.getVariable(0);
			if (clazz != variable.getClazz())
				continue;

			// these are double
			for (int i = 0; i < sol.getNumberOfObjectives(); i++) {
				bw.write(Double.toString(sol.getObjective(i)));
				bw.write('\t');
			}
			// now the fields
			for (int i = 0; i < variable.getNumberFields(); i++) {
				String field = variable.getField(i);
				bw.write(field);
				bw.write('\t');
			}
			// and finally the graph
			bw.write(variable.getText());
			bw.newLine();
		}
		bw.close();
	}

	private static String readResultsFile(ArrayList<Solution> solutions, String datafile, final int numberNonObjectiveColumns, int clazz)
			throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(datafile, StandardCharsets.UTF_8), 1 << 24);
		String line;
		boolean headRead = false;
		int numberObjectives = 0;
		String header = null;
		int solutionCounter = 0;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				continue;
			String[] cells = VariousUtils.fastSplit(line, "\t");
			if (!headRead) {
				headRead = true;
				numberObjectives = cells.length - numberNonObjectiveColumns;
				header = line;
				continue;
			} else {
				Solution solution = new Solution(1, numberObjectives);
				// class + blend
				StringVariable variable = new StringVariable(cells[cells.length - 1]);
				variable.setClazz(clazz);

				for (int i = 0; i < numberObjectives; i++) {
					double obj = Double.parseDouble(cells[i]);
					solution.setObjective(i, obj);
				}

				// store remaining fields except the graph
				for (int i = 0; i < numberNonObjectiveColumns - 1; i++) {
					String fieldData = cells[numberObjectives + i];
					variable.addField(fieldData);
				}

				solution.setVariable(0, variable);
				solutions.add(solution);
				solutionCounter++;
			}
		}
		br.close();
		System.out.println("read " + solutionCounter + " solutions from " + datafile);
		return header;
	}
}
