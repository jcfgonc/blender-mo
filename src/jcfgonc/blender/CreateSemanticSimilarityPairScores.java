package jcfgonc.blender;

import java.io.IOException;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import structures.MapOfList;
import structures.UnorderedPair;
import wordembedding.ListWordEmbedding;
import wordembedding.WordEmbeddingReadWrite;
import wordembedding.WordEmbeddingUtils;

public class CreateSemanticSimilarityPairScores {

	public static void main(String[] args) throws IOException {
		String wordembeddingFilename = MOEA_Config.wordembedding_filename;
		String synonymsFilename = MOEA_Config.synonyms_filename;
		String wordpairscoresFilename = MOEA_Config.wordPairScores_filename;

		// this code may be moved to the blender's launcher - but it has the problem of additional startup delay
		ListWordEmbedding we = WordEmbeddingReadWrite.readCSV(wordembeddingFilename, true);
		MapOfList<String, String> synonyms = WordEmbeddingUtils.readSynonymWordList(synonymsFilename, we);
		Object2DoubleOpenHashMap<UnorderedPair<String>> wps = WordEmbeddingUtils.scoreWordPairs(we, synonyms);
		WordEmbeddingUtils.saveWordPairScores(wps, wordpairscoresFilename);
	}

}
