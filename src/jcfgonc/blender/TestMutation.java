package jcfgonc.blender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.Well44497b;

import graph.StringGraph;
import jcfgonc.blender.structures.Blend;
import structures.Mapping;
import visual.GraphData;

/**
 * tests the blender mutation and shows the operations visually on a GUI
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class TestMutation {
	public static void testMutation(StringGraph inputSpace, List<Mapping<String>> mappings) throws InterruptedException {
		final int populationSize = 16;
		RandomAdaptor random = new RandomAdaptor(new Well44497b(0));
		Mapping<String> mapping = mappings.get(random.nextInt(mappings.size()));
		Semaphore stepSem = new Semaphore(1);

		ArrayList<Blend> blends = new ArrayList<Blend>(populationSize);
		ArrayList<GraphData> arrGD = new ArrayList<GraphData>(populationSize);
		for (int i = 0; i < populationSize; i++) {
			Blend b = new Blend(new StringGraph(), mapping, i);
			GraphData gd = new GraphData(Integer.toString(i), b.getBlendSpace());
			blends.add(b);
			arrGD.add(gd);
		}

		BlenderStepperGUI bs = new BlenderStepperGUI();
		bs.setup(arrGD, stepSem);
		arrGD = null;

		while (true) {
			// stepSem.acquire();
//			Thread.yield();
			Thread.sleep(1000);
			mutateBlends(blends);
			updateGraphs(bs, blends);
		}

		// System.lineSeparator();
	}

	private static void mutateBlends(ArrayList<Blend> blends) {
		blends.parallelStream().forEach(blend -> {
			BlendMutation.mutateBlend(blend);
		});
//		for (int i = 0; i < blends.size(); i++) {
//			Blend blend = blends.get(i);
//			BlendMutation.mutateBlend(random, blend, inputSpace);
//		}
	}

	private static void updateGraphs(BlenderStepperGUI bs, ArrayList<Blend> blends) {
		blends.parallelStream().forEach(blend -> {
			bs.updateBlendGraph(blend.getSelfId(), blend.getBlendSpace());
		});
//		for (int i = 0; i < blends.size(); i++) {
//			Blend blend = blends.get(i);
//			bs.updateBlendGraph(i, blend.getBlendSpace());
//		}
	}
}
