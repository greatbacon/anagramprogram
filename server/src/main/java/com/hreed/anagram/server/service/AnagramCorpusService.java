package com.hreed.anagram.server.service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

//By utilizing an interface for the service, 
//the application could be converted to use a DB or other medium for data storage down the road.
public interface AnagramCorpusService {
	
	/**Takes a set of words and adds them to the corpus if they don't already exist
	 * 
	 * @param newWords The collection of new words to be added
	 */
	public void addWords(Set<String> newWords);
	
	/**Given a word and limit value, return all anagrams for the given word, up to the limit provided.
	 * 
	 * @param word The word to be used for finding anagrams of
	 * @param limit Limit on the number of anagrams returned.  Returns all if null.
	 * @return The set of all (or potentially limited) anagrams of the provided word
	 */
	public Set<String> getAnagrams(String word, Integer limit);
	
	/**Given a word and limit value, return all anagrams regardless of capitalization for the given word, up to the limit provided.
	 * 
	 * @param word The word to be used for finding anagrams of
	 * @param limit Limit on the number of anagrams returned. Returns all if null.
	 * @return The set of all (or porentially limited) anagrams of the provided word, regardless of capitalized characters.
	 */
	public Set<String> getAnagramsCaseInsensitive(String word, Integer limit);
	
	/**
	 * Deletes the specified word from the corpus if it exists
	 * @param word The word to be deleted
	 */
	public void deleteWord(String word);
	
	/**
	 * Deletes the specified word and all of it's anagrams from the corpus.
	 * @param word
	 */
	public void deleteAnagrams(String word);
	
	/**
	 * Completly removes all words from the corpus
	 */
	public void deleteAllWords();

	/**Clears the current corpus,
	 * then loads an internal file from the relative path, given the name of the file
	 * 
	 * @param fileName The name of the text file to load into the corpus. Assumes the file is located at /src/main/resources
	 */
	public void populateCorpusFromDictionaryFile(String fileName);
	
	/**
	 * Returns the number of words in the current corpus, min,max,median, and average word length.
	 * Will return zero for all values if the dictionary is empty 
	 * @return The set of all metadata in a key,value collection.
	 */
	public Map<String, Object> getCorpusMetadata();
	
	/**
	 * Returns a list of the largest anagram sets. In the event of a tie for size, multiple sets are returned.
	 * If the dictionary is empty, an empty ArrayList is returned.
	 * @return An ArrayList containing zero, one, or many sets of anagrams.
	 */
	public ArrayList<Set<String>> getLargestAnagramSets();
	
	/**
	 * Returns a list of all anagram sets of an equal or greater size than the provided value.
	 * Will return all available anagram sets for values of one or less
	 * 
	 * @param size The minimum size of all anagram sets to be returned.
	 * @return A list of all anagram sets of size equal or greater than the provided value
	 */
	public ArrayList<Set<String>> getAnagramGroupsBySize(int size);
	
}
