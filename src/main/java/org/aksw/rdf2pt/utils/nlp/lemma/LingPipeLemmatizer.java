package org.aksw.rdf2pt.utils.nlp.lemma;

import java.util.ArrayList;
import java.util.List;

import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;

public class LingPipeLemmatizer implements Lemmatizer {

	public LingPipeLemmatizer() {
	}

	@Override
	public String stem(String word) {
		return PorterStemmerTokenizerFactory.stem(word);
	}

	@Override
	public String stem(String word, String tag) {
		return PorterStemmerTokenizerFactory.stem(word);
	}

	@Override
	public List<String> stem(List<String> words) {
		List<String> stemmedWords = new ArrayList<String>();
		for(String word : words){
			stemmedWords.add(stem(word));
		}
		return stemmedWords;
	}

}
