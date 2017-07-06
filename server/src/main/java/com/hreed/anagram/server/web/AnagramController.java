package com.hreed.anagram.server.web;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hreed.anagram.server.service.AnagramCorpusService;

@RestController
public class AnagramController {

	@Autowired
	private AnagramCorpusService anagramCorpusService;
	
	@RequestMapping(value = "/words",method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	public void addWords(@RequestBody Set<String> words) throws Exception {
		System.out.println(words.toString());
	}
	
	
}
