package jcfgonc.blender.structures;

import java.math.BigInteger;

import graph.StringGraph;
import structures.Mapping;

public class Blend {
	private StringGraph blendSpace;
	private int selfid;
	private int parentid;
	private Mapping<String> mapping;
	private static int idCounter = 1; // 0 is reserved to doG

	public Blend(StringGraph blendSpace, Mapping<String> mapping, int parentid) {
		this.blendSpace = blendSpace;
		this.mapping = mapping;
		this.selfid = idCounter;
		this.parentid = parentid;
		Blend.idCounter++;
	}

	public StringGraph getBlendSpace() {
		return blendSpace;
	}

	/**
	 * Returns the unique ID for this blend.
	 * 
	 * @return
	 */
	public int getId() {
		return selfid;
	}

	public Mapping<String> getMapping() {
		return mapping;
	}

	public String toString() {
		return blendSpace.toString();
	}

	/**
	 * Returns the ID of this blend's ancestor/parent (currently there is no support for multiple parents/crossover).
	 * 
	 * @return
	 */
	public int getParentId() {
		return parentid;
	}

	public int hashcode() {
		int hashcode;
		// the hashcode of a blend depends on its blend space and on its mapping
		BigInteger h = BigInteger.valueOf(blendSpace.hashCode());
		h.shiftLeft(32);
		h.add(BigInteger.valueOf(mapping.hashCode()));
		hashcode = h.hashCode();

	//	hashcode = blendSpace.hashCode() * 31 + mapping.hashCode();

		return hashcode;
	}
}
