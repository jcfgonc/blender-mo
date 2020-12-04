package jcfgonc.blender.structures;

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

	public int getSelfId() {
		return selfid;
	}

	public Mapping<String> getMapping() {
		return mapping;
	}

	public String toString() {
		return blendSpace.toString();
	}

	public int getParentId() {
		return parentid;
	}
}
