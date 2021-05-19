package jcfgonc.moea.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import graph.StringGraph;
import jcfgonc.blender.MOEA_Config;
import jcfgonc.blender.logic.FileTools;
import structures.Mapping;

public class MappingTreatment {
	public static void main(String[] args) throws IOException {
		// read input space
		StringGraph inputSpace = FileTools.readInputSpace(MOEA_Config.inputSpacePath);

		// remove (currently) useless relations
//	System.out.print("removing (currently) useless relations... ");
//	inputSpace.removeEdges("derivedfrom");
//	inputSpace.removeEdges("hascontext");
//	inputSpace.removeEdges("relatedto");
//	inputSpace.removeEdges("synonym");
//	inputSpace.removeEdges("antonym");
//	inputSpace.removeEdges("similarto");
//	System.out.println("done.");

		// read mappings file (contains multiple mappings)
		ArrayList<Mapping<String>> mappings = Mapping.readMappingsCSV(new File(MOEA_Config.mappingPath));

//		for (Mapping<String> mapping : mappings) {
//			mapping.renameConcept("peron", "person");
//		}
//		Mapping.writeMappingsCSV(mappings, "map.csv");

		showMappingProblems(inputSpace, mappings);
		System.exit(0);
	}

	@SuppressWarnings("unused")
	private static void showMappingProblems(StringGraph inputSpace, ArrayList<Mapping<String>> mappings) {
		Set<String> vertexSet = inputSpace.getVertexSet();
		HashSet<String> missingISconcepts = new HashSet<String>();
		HashSet<String> mappingConcepts = new HashSet<String>();
		for (Mapping<String> mapping : mappings) {
			mappingConcepts.addAll(mapping.getLeftConcepts());
			mappingConcepts.addAll(mapping.getRightConcepts());
		}
		for (String mappingConcept : mappingConcepts) {
			if (!vertexSet.contains(mappingConcept)) {
				System.out.println("input space does not have the concept " + mappingConcept + " present in the mappings");
				missingISconcepts.add(mappingConcept);
			}
		}

		// show concept pairs containing the missing concept(s)
		for (String missing : missingISconcepts) {
			for (Mapping<String> mapping : mappings) {
				if (mapping.containsConcept(missing)) {
					System.out.println(mapping.getConceptPair(missing));
				}
			}
		}
	}
}
