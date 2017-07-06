package com.hreed.anagram.server.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.hreed.anagram.server.service.AnagramCorpusService;

@Component
public class AnagramCorpusServiceInMemory implements AnagramCorpusService {
	
	Map<String,Set<String>> corpus;
	
	public AnagramCorpusServiceInMemory(){
		corpus = new HashMap<String,Set<String>>();
	}

	@Override
	public void addWords(Set<String> newWords) {		
		Iterator<String> newWordsIterator = newWords.iterator();
		while (newWordsIterator.hasNext()){
			String word = newWordsIterator.next();
			String key = generateKey(word);
			corpus.get(key).add(word);
		}
	}

	@Override
	public Set<String> getAnagrams(String word) {
		String key = generateKey(word);
		Set<String> anagrams = corpus.get(key);
		anagrams.remove(word);
		return anagrams;
	}

	//In order to create a standard key across words, 
	//first the string is converted into a charArray, unicode sorted, then converted back into a string. 
	private String generateKey(String word){
		char[] wordArray = word.toCharArray();
		Arrays.sort(wordArray);
		return new String(wordArray);
	}
}
