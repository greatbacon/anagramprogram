package com.hreed.anagram.server.service;

import java.util.Set;

//By utilizing an interface for the service, 
//the application could be converted to use a DB or other medium for data storage down the road.
public interface AnagramCorpusService {
	
	/**Takes a set of words and adds them to the corpus if they don't already exist
	 * 
	 * @param newWords The collection of new words to be added
	 */
	public void addWords(Set<String> newWords);
	
	/**
	 * 
	 * @param word The word to be used for finding anagrams of
	 * @param limit Limit on the number of anagrams returned.  Returns all if null.
	 * @return The set of all (or potentially limited) anagrams of the provided word
	 */
	public Set<String> getAnagrams(String word, Integer limit);
	
	/**
	 * Deletes the specified word from the corpus if it exists
	 * @param word The word to be deleted
	 */
	public void deleteWord(String word);
	
	/**
	 * Completly removes all words from the corpus
	 */
	public void deleteAllWords();

	/**Clears the current corpus from memory,
	 * then loads an internal file from the relative path, given the name of the file
	 * 
	 * @param fileName The name of the text file to load into the corpus. Assumes the file is located at /src/main/resources
	 */
	public void populateCorpusFromDictionaryFile(String fileName);
}
