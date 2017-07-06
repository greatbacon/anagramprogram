package com.hreed.anagram.server.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
			//If there is already a word set for a given key, try and add the new word
			if (corpus.get(key)!= null){
				corpus.get(key).add(word);
				System.out.println("Key match, adding new word");
			} else {
			//If there isn't a set for a given key, initialize the hashset
				Set<String> newSet = new HashSet<String>();
				newSet.add(word);
				corpus.put(key, newSet);
				System.out.println("New Key, adding new set");
			}
		}
	}

	@Override
	public Set<String> getAnagrams(String word) {
		Set<String> anagrams = new HashSet<String>();
		String key = generateKey(word);
		Set<String> result = corpus.get(key); 
		//Assuming a word set was found for the key, return the set minus the searched for word
		if (result != null){
			anagrams.addAll(result);
			anagrams.remove(word);
		}
		//Either return a discovered set, minus utilized word or an empty set
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
