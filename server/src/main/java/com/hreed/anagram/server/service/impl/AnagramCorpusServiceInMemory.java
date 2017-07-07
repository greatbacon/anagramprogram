package com.hreed.anagram.server.service.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.hreed.anagram.server.service.AnagramCorpusService;

@Component
public class AnagramCorpusServiceInMemory implements AnagramCorpusService {
	
	private Map<String,Set<String>> corpus;
	private Logger log = Logger.getLogger(this.getClass());
	
	public AnagramCorpusServiceInMemory(){
		//TODO update with ConcurrentHashMap
		corpus = new HashMap<String,Set<String>>();
		//TODO replace value with one from properties file
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
	/**Generates a key for the corpus by converting the string into a charArray, sorting it,
	 * then converting it back into a string
	 * 
	 * @param word The word used to create the key
	 * @return The resulting key
	 */
	private String generateKey(String word){
		char[] wordArray = word.toCharArray();
		Arrays.sort(wordArray);
		return new String(wordArray);
	}
	
	/**Inserts a word into the HashMap used for storing the corpus.
	 * 
	 * @param word The word to be added to the HashMap
	 */
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
	
	public void populateCorpusFromDictionaryFile(String fileName){
		deleteAllWords();
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

	@Override
	public void deleteWord(String word) {
		String key = generateKey(word);
		Set<String> result = corpus.get(key);
		//If there is a result for the key remove, otherwise it does not exist and does not need to be removed
		if (result != null){
			result.remove(word);
		}
	}

	@Override
	public void deleteAllWords() {
		corpus.clear();		
	}

	@Override
	public Map<String, Object> getCorpusMetadata() {
		Map<String, Object> results = new HashMap<String, Object>();
		//Perform a copy of the current state of the corpus, to ensure the current analysis doesn't fail if it changes
		Map<String,Set<String>> corpusState = new HashMap<String,Set<String>>(corpus);
		Iterator<Entry<String, Set<String>>> corpusIterator = corpusState.entrySet().iterator();
		ArrayList<Integer> medianCollection = new ArrayList<Integer>();
		DecimalFormat decimalFormatter = new DecimalFormat("#.###");
		int totalWordLength = 0;
		int wordCount = 0;
		int minWordLength = Integer.MAX_VALUE;
		int maxWordLength = Integer.MIN_VALUE;
		//If there is no collection of words to actually sort through
		if (!corpusIterator.hasNext()){
			results.put("word_count", 0);
			results.put("min_length", 0);
			results.put("max_length", 0);
			results.put("median_length", 0);
			results.put("avg_length", 0);
		} else {
			while (corpusIterator.hasNext()){
				Entry<String, Set<String>> corpusEntry = corpusIterator.next();
				Iterator<String> words = corpusEntry.getValue().iterator();
				while (words.hasNext()){
					String word = words.next();
					//increment total word count
					wordCount++;
					
					int wordLength = word.length();
					//add to the total count of all word lengths (for average computations)
					totalWordLength += wordLength;
					//determine if the word is the smallest word in the new corpus
					if (wordLength < minWordLength){
						minWordLength = wordLength;
					}
					//determine if the word is the largest word in the new corpus
					if (wordLength > maxWordLength){
						maxWordLength = wordLength;
					}
					medianCollection.add(wordLength);				
				}
			}
			results.put("word_count", wordCount);
			results.put("min_length", minWordLength);
			results.put("max_length", maxWordLength);
			results.put("median_length", decimalFormatter.format(calculateMean(medianCollection)));
			results.put("avg_length", decimalFormatter.format(((double) totalWordLength / (double) wordCount)));
		}		
		
		return results;
	}
	
	private double calculateMean(ArrayList<Integer> medianCollection){
		double result = 0.0;
		medianCollection.sort(null);
		int collectionSize = medianCollection.size();
		if (collectionSize > 0){
			//the set of integers is odd, so grab the middle one as the mean
			if (collectionSize % 2 == 1){
				result = medianCollection.get((collectionSize-1)/2);
			}
			//the set of integers is even, so we must calculate the mean
			else {
				int firstValue = medianCollection.get(collectionSize/2);
				int secondValue = medianCollection.get((collectionSize/2)-1);
				result = ((double) firstValue + (double) secondValue) / 2;
			}
		}
		return result;
	}

}
