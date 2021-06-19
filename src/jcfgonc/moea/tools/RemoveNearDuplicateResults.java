package jcfgonc.moea.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;

import org.moeaframework.core.Solution;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import utils.VariousUtils;

/**
 * Removes coincident or nearby (in terms of objective coordinates) blends. Works with two classes (with/without frames)-
 * 
 * @author "Joao Goncalves: jcfgonc@gmail.com"
 *
 */
public class RemoveNearDuplicateResults {

	public static void main(String[] args) throws IOException {
		// --- BLENDERMO results HEADER
		// first columns are the objectives
		// last 4 columns are
		// d:graph's vertices
		// d:graph's edges
		// f:novelty
		// g:blend space
		final int numberNonObjectiveColumns = 6;
		String resultsFilename0 = "..\\BlenderMO\\results\\study 2\\not using frames_nondup.tsv";
		String resultsFilename1 = "..\\BlenderMO\\results\\study 2\\using frames ss unrestricted.tsv";

		ArrayList<Solution> solutions = new ArrayList<Solution>();
		String header0 = RemoveDominatedResults.readResultsFile(solutions, resultsFilename0, numberNonObjectiveColumns, 0);
		String header1 = RemoveDominatedResults.readResultsFile(solutions, resultsFilename1, numberNonObjectiveColumns, 1);

		System.out.println("read a total of " + solutions.size() + " solutions");

		BitSet solutionsToRemove = new BitSet(solutions.size());

		// find duplicated/neighboring solutions
		for (int i = 0; i < solutions.size() - 1; i++) {
			Solution s0 = solutions.get(i);
			for (int j = i + 1; j < solutions.size(); j++) {
				if (solutionsToRemove.get(j))
					continue;

				Solution s1 = solutions.get(j);

				double o0 = s0.getObjective(0);
				double o1 = s1.getObjective(0);

				// simple loop break out
				double abs = Math.abs(o0 - o1);
				if (abs < 1e-9) {
					// if solutions near enough then
					double dist = calculateDistance(s0, s1);
					// filter similar/neighbor solutions
					if (dist < 1e-6) {
						solutionsToRemove.set(j);
					}
				}
			}
		}
		System.out.printf("%d duplicated solutions\n", solutionsToRemove.cardinality());

		// remove duplicated solutions
		ArrayList<Solution> solutionsNew = new ArrayList<Solution>(solutions.size());
		for (int i = 0; i < solutions.size(); i++) {
			if (solutionsToRemove.get(i))
				continue;
			solutionsNew.add(solutions.get(i));
		}
		System.out.println(solutionsNew.size() + " unique solutions");

		Int2IntOpenHashMap classCounter = new Int2IntOpenHashMap();
		for (int i = 0; i < solutionsNew.size(); i++) {
			Solution solution = solutionsNew.get(i);
			StringVariable variable = (StringVariable) solution.getVariable(0);
			classCounter.addTo(variable.getClazz(), 1);
		}
		System.out.println("class cardinality = " + classCounter);
//		// remove dominated solutions
//		// put the solutions in a ndp
//		NondominatedPopulation pop = new NondominatedPopulation();
//		for (Solution solution : solutions) {
//			pop.add(solution);
//		}

//		System.out.println("read " + initialNumberSolutions + " solutions");
//		System.out.println("NondominatedPopulation contains " + solutions.size() + " solutions");
		saveResultsFile(solutionsNew, VariousUtils.appendSuffixToFilename(resultsFilename0, "_nondup"), header0, 0);
		saveResultsFile(solutionsNew, VariousUtils.appendSuffixToFilename(resultsFilename1, "_nondup"), header1, 1);
	}

	private static double calculateDistance(Solution s0, Solution s1) {
		double accum = 0;
		for (int i = 0; i < s0.getNumberOfObjectives(); i++) {
			double o0 = s0.getObjective(i);
			double o1 = s1.getObjective(i);
			double dif = o0 - o1;
			accum += dif * dif;
		}
		return accum;
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
}
