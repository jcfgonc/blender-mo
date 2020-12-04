package jcfgonc.blender.logic;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.math3.random.RandomGenerator;

import graph.EdgeDirection;
import graph.StringEdge;
import graph.StringGraph;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import structures.ObjectIndex;
import utils.VariousUtils;

public class CompressionUtils {

	public static void main(String[] args) {
		int[] a = { 1, 2, 1, 2, 3, 3, 3, 1, 2, 3, 1, 2, 3, 5, 1, 2, 3, 6, 1, 2, 3 };
		System.out.println(patternCompressor(new IntArrayList(a), 2));
	}

	public static IntArrayList patternCompressor(IntArrayList initialArray, int minPatternSize) {
		IntArrayList array = initialArray;
		int n = array.size();
		// max repeating sequence will be n/2
		for (int patternSize = n / 2; patternSize >= minPatternSize; patternSize--) {
			// start a sequence to find
			for (int patternStart = 0; patternStart + patternSize <= n; patternStart++) {
				int patternEnd = patternStart + patternSize;
				// no space left for matching
				if (patternEnd > n - patternSize)
					break;
				// find the sequence in the remaining array
				for (int i = patternEnd; i + patternSize <= n; i++) {
					boolean exists = compare(array, patternStart, patternEnd, i, i + patternSize);
					// if exists, add to list of cuts and jump subseqsize elements
					if (exists) {
						// corta e anda para tras
						array = joinTwoParts(array, 0, i, i + patternSize, n);
						n = array.size();
						i--;
					}
				}
			}
		}
		return array;
	}

	/**
	 * true if equal between [i0...i1[ and [j0...j1[
	 * 
	 * @param array
	 * @param i0    inclusive
	 * @param i1    exclusive
	 * @param j0    inclusive
	 * @param j1    exclusive
	 * @return
	 */
	private static boolean compare(IntArrayList array, int i0, int i1, int j0, int j1) {
		int i = i0;
		int j = j0;
		while (i < i1 || j < j1) {
			if (array.getInt(i) != array.getInt(j))
				return false;
			i++;
			j++;
		}
		return true;
	}

	/**
	 * Joints subelements from array starting at i0 to i1 with elements starting at j0 to j1
	 * 
	 * @param array
	 * @param i0    inclusive
	 * @param i1    exclusive
	 * @param j0    inclusive
	 * @param j1    exclusive
	 * @return
	 */
	private static IntArrayList joinTwoParts(IntArrayList array, int i0, int i1, int j0, int j1) {
		int n = i1 - i0 + j1 - j0;
		IntArrayList newArray = new IntArrayList(n);
		for (int i = i0; i < i1; i++) {
			newArray.add(array.getInt(i));
		}
		for (int j = j0; j < j1; j++) {
			newArray.add(array.getInt(j));
		}
		return newArray;
	}

	public static int scoreCompression(IntArrayList sequence) {
		try {
			ByteArrayList bufferRelation = new ByteArrayList();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			{
				BufferedOutputStream bos = new BufferedOutputStream(baos);
				DeflaterOutputStream compressedOS = new DeflaterOutputStream(bos);
				// BZip2CompressorOutputStream compressedOS = new BZip2CompressorOutputStream(bos);
				// ZipOutputStream compressedOS = new ZipOutputStream(bos);
				// compressedOS.putNextEntry(new ZipEntry("0"));

				for (int relationId : sequence) {
					bufferRelation.add((byte) relationId);
				}
				compressedOS.write(bufferRelation.toByteArray());
				compressedOS.finish();
				compressedOS.close();
				bos.close();
			}
			baos.close();
			int size = baos.size();
			return size;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.err.println("SERIOUS PROBLEM - CHECK");
		return -1;
	}

	public static IntArrayList iterativeExpansionBuilder(StringGraph outputSpace, boolean breadth, RandomGenerator random) {
		ObjectIndex<String> relationLabels = new ObjectIndex<>();
		IntArrayList relationSequence = new IntArrayList();

		HashSet<String> openSetGlobal = new HashSet<>(outputSpace.getVertexSet());
		ArrayDeque<String> openSetLocal = new ArrayDeque<>();
		HashSet<String> closedSet = new HashSet<>();

		boolean booting = false;

		while (!openSetGlobal.isEmpty()) {
			String currentVertex;
			if (booting) {
				currentVertex = VariousUtils.getRandomElementFromCollection(openSetGlobal, random);
			} else {
				currentVertex = openSetGlobal.iterator().next();
			}

			if (breadth) {
				openSetLocal.add(currentVertex);
			} else {
				openSetLocal.push(currentVertex);
			}

			while (!openSetLocal.isEmpty()) {
				if (breadth) {
					currentVertex = openSetLocal.remove();
				} else {
					currentVertex = openSetLocal.pop();
				}
				openSetGlobal.remove(currentVertex);
				// expand a single vertex not in the closed set
				if (!closedSet.contains(currentVertex)) {
					IntArrayList localRelationSequence = new IntArrayList();
					// get the vertex neighbors not in the closed set
					for (String neighbor : outputSpace.getNeighborVertices(currentVertex)) {
						if (closedSet.contains(neighbor))
							continue;
						// put the neighbors in the open set
						if (breadth) {
							openSetLocal.add(neighbor);
						} else {
							openSetLocal.push(neighbor);
						}
						// TODO not sure if this is bidirectional
						for (StringEdge edge : outputSpace.getUndirectedEdgesConnecting(currentVertex, neighbor)) {
							EdgeDirection dir = StringGraph.getEdgeDirectionRelativeTo(currentVertex, edge);
							String relation = edge.getLabel();
							int relationId = relationLabels.addObject(relation);
							if (dir != EdgeDirection.OUTGOING) {
								relationId = -relationId;
							}
							// TODO: experimentar colocar isto no remove/pop
							localRelationSequence.add(relationId);
						}
					}
					localRelationSequence.sort(null);
					relationSequence.addAll(localRelationSequence);
					// vertex from the open set explored, remove it from further exploration
					closedSet.add(currentVertex);
				}
			}
		}
		return relationSequence;
	}

	public static IntArrayList newIntArrayList(IntArrayList ial, int from, int to) {
		IntArrayList ret = new IntArrayList();
		for (int i = from; i < to; i++) {
			ret.add(ial.getInt(i));
		}
		return ret;
	}

}
