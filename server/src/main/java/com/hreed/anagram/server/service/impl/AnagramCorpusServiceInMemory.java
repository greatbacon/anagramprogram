package com.hreed.anagram.server.service.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.hreed.anagram.server.service.AnagramCorpusService;

@Component
public class AnagramCorpusServiceInMemory implements AnagramCorpusService {
	
	private Map<String,Set<String>> corpus;
	private Logger log = Logger.getLogger(this.getClass());
	
	public AnagramCorpusServiceInMemory(){
		corpus = new HashMap<String,Set<String>>();
		populateCorpusFromDictionaryFile("/dictionary.txt");
	}

	@Override
	public void addWords(Set<String> newWords) {		
		Iterator<String> newWordsIterator = newWords.iterator();
		while (newWordsIterator.hasNext()){
			insertWord(newWordsIterator.next());
		}
	}

	@Override
	public Set<String> getAnagrams(String word, Integer limit) {
		Set<String> anagrams = new HashSet<String>();
		String key = generateKey(word);
		Set<String> result = corpus.get(key); 
		//Assuming a word set was found for the key, return the set minus the searched for word
		if (result != null){						
				anagrams.addAll(result);
				anagrams.remove(word);
		    //if a limit on response needs to be applied
			if (limit != null){				
				Iterator<String> resultIterator = anagrams.iterator();
				Set<String> limitedAnagrams = new HashSet<String>();
				for (int i = 0; i < limit; i++) {
					if (resultIterator.hasNext()){
						limitedAnagrams.add(resultIterator.next());						
					}
				}
				anagrams = limitedAnagrams;
			}					
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
	
	private void insertWord(String word){		
		String key = generateKey(word);
		//If there is already a word set for a given key, try and add the new word
		if (corpus.get(key)!= null){
			corpus.get(key).add(word);
			log.debug("Key match, adding new word : " + word);
		} else {
		//If there isn't a set for a given key, initialize the hashset
			Set<String> newSet = new HashSet<String>();
			newSet.add(word);
			corpus.put(key, newSet);
			log.debug("New Key, adding new set for word : " + word);
		}
	}
	
	//This method assumes the file can be found on the relative path
	public void populateCorpusFromDictionaryFile(String fileName){
		log.info("Loading dictionary file `"+fileName+"` at startup");
		String word;
		try {
			InputStream inputStream = getClass().getResourceAsStream(fileName);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			
			while ((word=bufferedReader.readLine())!=null){
				insertWord(word);
			}
		} catch (FileNotFoundException e) {
			log.error("Unable to locate dictionary file `"+fileName+"` on class path. Dictionary not loaded.");
			
		} catch (IOException e) {
			log.error("Error processing dictionary file `"+fileName+"`. Is it formatted properly?");
		}
	}
}
