package com.hreed.anagram.server.web;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hreed.anagram.server.service.AnagramCorpusService;

@RestController
public class AnagramController {

	@Autowired
	private AnagramCorpusService anagramCorpusService;
	
	@RequestMapping(value = "/words.json",method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	public void addWords(@RequestBody Map<String, Object> payload) throws Exception {		
		Set<String> newWords = new HashSet<String>((Collection<String>) payload.get("words"));
		anagramCorpusService.addWords(newWords);
	}
	
	@RequestMapping(value = "/anagrams/{word}.json",method = RequestMethod.GET)
	public Map<String, Object> getAnagrams(@PathVariable("word") String word, 
			@RequestParam(value="limit",required=false) Integer limit) {
		Map<String, Object> response = new HashMap<String, Object>();
		Set<String> anagrams = anagramCorpusService.getAnagrams(word,limit);
		response.put("anagrams", anagrams);
		return response;
	}
	
}
