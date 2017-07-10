package com.hreed.anagram.server.web;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	private Logger log = Logger.getLogger(this.getClass());
	
	
	@RequestMapping(value = "/words.json",method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	public void addWords(@RequestBody Map<String, Object> payload) throws Exception {		
		Set<String> newWords = new HashSet<String>((Collection<String>) payload.get("words"));
		anagramCorpusService.addWords(newWords);
	}
	
	@RequestMapping(value = "/anagrams/{word}.json",method = RequestMethod.GET)
	public Map<String, Object> getAnagrams(@PathVariable("word") String word, 
			@RequestParam(value="limit",required=false) String limitQuery, 
			@RequestParam(value="caseinsensitive",required=false) String caseInsensitive) {
		//Check to see if the limit flag was set to a valid integer
		Integer value = null;
		if (limitQuery != null){
			try {
				value = Integer.parseInt(limitQuery);
			} catch (NumberFormatException e){
				log.error("Failed to parse int from limit param : "+limitQuery +". Defaulting to no limit.");
			}
		}
		Boolean includeCapitals = false;
		if (caseInsensitive != null){
			includeCapitals = Boolean.parseBoolean(caseInsensitive);
		}
		Map<String, Object> response = new HashMap<String, Object>();
		Set<String> anagrams;
		if (includeCapitals == false){
			anagrams = anagramCorpusService.getAnagrams(word,value);	
		} else {
			anagrams = anagramCorpusService.getAnagramsCaseInsensitive(word, value);
		}
		
		response.put("anagrams", anagrams);
		return response;
	}
	
	@RequestMapping(value = "/anagrams/{word}.json",method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void deleteAnagrams(@PathVariable("word") String word){
		anagramCorpusService.deleteAnagrams(word);
	}
	
	@RequestMapping(value = "/words.json",method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void deleteAllWords() {
		anagramCorpusService.deleteAllWords();
	}
	
	@RequestMapping(value = "/words/{word}.json",method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void deleteWord(@PathVariable("word") String word){
		anagramCorpusService.deleteWord(word);
	}
	
	@RequestMapping(value = "/reload.json",method = RequestMethod.GET)
	public void reloadDictionary(){		
		anagramCorpusService.populateCorpusFromDictionaryFile("/dictionary.txt");
	}
	
	@RequestMapping(value = "/metadata.json",method = RequestMethod.GET)
	public Map<String, Object> getDictionaryMetadata(){
		Map<String, Object> response = new HashMap<String, Object>();
		response = anagramCorpusService.getCorpusMetadata();
		return response;
	}
	
	@RequestMapping(value = "/most.json",method = RequestMethod.GET)
	public Map<String, Object> getLargestAnagramSets(){
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("anagrams", anagramCorpusService.getLargestAnagramSets());
		return response;
	}
	
	@RequestMapping(value = "groups.json",method = RequestMethod.GET)
	public Map<String, Object> getAnagramGroupsBySize(@RequestParam("size") String size, HttpServletResponse response){
		Map<String, Object> result = new HashMap<String, Object>();
		Integer value = null;
		if (size != null){
			try {
				value = Integer.parseInt(size);
				result.put("anagrams", anagramCorpusService.getAnagramGroupsBySize(value));
			} catch (NumberFormatException e){
				log.error("Failed to parse int from size param : "+size +". Returning bad request.");
				result.put("success", false);
				result.put("message", "Invalid value for size param");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		}		
		return result;
	}
	
}
