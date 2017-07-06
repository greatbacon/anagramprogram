package com.hreed.anagram.server.service;

import java.util.Set;

//By utilizing an interface for the service, 
//the application could be converted to use a DB or other medium for data storage down the road.
public interface AnagramCorpusService {
	
	public void addWords(Set<String> newWords);
	
	public Set<String> getAnagrams(String word, Integer limit);
	
	public void deleteWord(String word);
	
	public void deleteAllWords();

}
