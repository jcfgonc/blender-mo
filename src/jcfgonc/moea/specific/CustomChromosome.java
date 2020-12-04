package jcfgonc.moea.specific;

import org.moeaframework.core.Variable;

import graph.StringGraph;
import jcfgonc.blender.BlendMutation;
import jcfgonc.blender.structures.Blend;
import structures.Mapping;

/**
 * This class represents the problem domain X, as a single dimension stored on a single custom gene.
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class CustomChromosome implements Variable {

	private static final long serialVersionUID = 1449562469642194509L;
	private Blend blend;

	public CustomChromosome(Blend b) {
		super();
		this.blend = b;
	}

	@Override
	/**
	 * Invoked by MOEA when copying a solution. Custom code required here.
	 */
	public CustomChromosome copy() {
		StringGraph blendSpace = blend.getBlendSpace();
		Mapping<String> mapping = blend.getMapping();
		int myid = blend.getSelfId();
		Blend childBlend = new Blend(new StringGraph(blendSpace), mapping, myid);

		CustomChromosome childc = new CustomChromosome(childBlend);
		return childc;
	}

	public Blend getBlend() {
		return blend;
	}

	/**
	 * Invoked by CustomMutation.evolve(). Custom code required here.
	 */
	public void mutate() {
		BlendMutation.mutateBlend(blend);
	}

	@Override
	/**
	 * Invoked by MOEA framework when creating a new solution/chromosome/variable. Custom code required here.
	 */
	public void randomize() {
		BlendMutation.mutateBlend(blend);
	}

	@Override
	/**
	 * Custom toString() code helpful here.
	 */
	public String toString() {
		if (blend == null) {
			return "null";
		}
		return blend.toString();
	}

}
