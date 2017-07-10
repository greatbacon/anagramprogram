#!/usr/bin/env ruby

require 'json'
require_relative 'anagram_client'
require 'test/unit'

# capture ARGV before TestUnit Autorunner clobbers it

class TestCases < Test::Unit::TestCase

  # runs before each test
  def setup
    @client = AnagramClient.new(ARGV)

    # add words to the dictionary
    @client.post('/words.json', nil, {"words" => ["read", "dear", "dare"] }) rescue nil
  end

  # runs after each test
  def teardown
    # delete everything
    @client.delete('/words.json') rescue nil
  end
  
  def test_file_ingestion
    
	# refresh dataset
	res = @client.get('/reload.json')
	assert_equal('200', res.code, "Unexpected response code")
	
	# fetch anagrams
    res = @client.get('/anagrams/read.json')

    assert_equal('200', res.code, "Unexpected response code")
    assert_not_nil(res.body)

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(ared daer dare dear)
    assert_equal(expected_anagrams, body['anagrams'].sort)
  end
  
  def test_file_ingestion_multiple_times
	3.times do
      # refresh dataset
	  res = @client.get('/reload.json')
      assert_equal('200', res.code, "Unexpected response code")
    end
	
	# fetch anagrams
    res = @client.get('/anagrams/read.json')

    assert_equal('200', res.code, "Unexpected response code")
    assert_not_nil(res.body)

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(ared daer dare dear)
    assert_equal(expected_anagrams, body['anagrams'].sort)
	
  end

  def test_adding_words
    res = @client.post('/words.json', nil, {"words" => ["read", "dear", "dare"] })

    assert_equal('201', res.code, "Unexpected response code")
  end
  
  def test_adding_no_words
	res = @client.post('/words.json', nil, {"words" => []})
	
	assert_equal('201', res.code, "Unexpected response code")
  end
  
  def test_adding_special_character_words
	#attempt to add words with invalid characters
	res = @client.post('/words.json', nil, {"words" => ["r&ad", "d&ar", "dar&"] })

    assert_equal('201', res.code, "Unexpected response code")
	
	#check if any anagrams were added
	res = @client.get('/anagrams/r&ad.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
  end
  
  def test_adding_proper_nouns
	#attempt to add words with invalid characters
	res = @client.post('/words.json', nil, {"words" => ["Dear", "Dare"] })

    assert_equal('201', res.code, "Unexpected response code")
	
	#check if any anagrams were added
	res = @client.get('/anagrams/Dear.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(Dare)
    assert_equal(expected_anagrams, body['anagrams'].sort)
  end
  
  def test_adding_non_standard_capitalization_words
	#attempt to add words with invalid characters
	res = @client.post('/words.json', nil, {"words" => ["rEad", "dEar", "darE"] })

    assert_equal('201', res.code, "Unexpected response code")
	
	#check if any anagrams were added
	res = @client.get('/anagrams/rEad.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
  end

  def test_fetching_anagrams    
    # fetch anagrams
    res = @client.get('/anagrams/read.json')

    assert_equal('200', res.code, "Unexpected response code")
    assert_not_nil(res.body)

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(dare dear)
    assert_equal(expected_anagrams, body['anagrams'].sort)
	
	# fetch a different anagram in the same set
    res = @client.get('/anagrams/dare.json')

    assert_equal('200', res.code, "Unexpected response code")
    assert_not_nil(res.body)

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(dear read)
    assert_equal(expected_anagrams, body['anagrams'].sort)
  end

  def test_fetching_anagrams_with_limit    
    # fetch anagrams with limit
    res = @client.get('/anagrams/read.json', 'limit=1')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(1, body['anagrams'].size)
  end
  
  def test_fetching_anagrams_with_limit_zero	
	# fetch anagrams with limit
    res = @client.get('/anagrams/read.json', 'limit=0')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
  end
	
  def test_fetching_anagrams_with_limit_under_zero	
	# fetch anagrams with limit
    res = @client.get('/anagrams/read.json', 'limit=-1')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
  end
  
  def test_fetching_anagrams_with_limit_above_set_size    
	# fetch anagrams with limit
    res = @client.get('/anagrams/read.json', 'limit=5')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(dare dear)
    assert_equal(expected_anagrams, body['anagrams'].sort)
  end
  
  def test_fetching_anagrams_with_non_number_limit
	res = @client.get('/anagrams/read.json', 'limit=xxyxxy')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(dare dear)
    assert_equal(expected_anagrams, body['anagrams'].sort)
  end	

  def test_fetch_for_word_with_no_anagrams
    # fetch anagrams with no anagrams
    res = @client.get('/anagrams/zyxwv.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
  end

  def test_deleting_all_words    

    res = @client.delete('/words.json')

    assert_equal('204', res.code, "Unexpected response code")

    # should fetch an empty body
    res = @client.get('/anagrams/read.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
  end

  def test_deleting_all_words_multiple_times    

    3.times do
      res = @client.delete('/words.json')

      assert_equal('204', res.code, "Unexpected response code")
    end

    # should fetch an empty body
    res = @client.get('/anagrams/read.json', 'limit=1')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
  end

  def test_deleting_single_word    

    # delete the word
    res = @client.delete('/words/dear.json')

    assert_equal('204', res.code, "Unexpected response code")

    # expect it not to show up in results
    res = @client.get('/anagrams/read.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(['dare'], body['anagrams'])
  end
  
  def test_deleting_nonexistant_word    

    # delete the word
    res = @client.delete('/words/zyxwv.json')

    assert_equal('204', res.code, "Unexpected response code")

  end
  
  def test_metadata_manual    
  
    #add more words to dictionary
    res = @client.post('/words.json', nil, {"words" => ["least", "slate", "stale", "steal", "tales"] })
    assert_equal('201', res.code, "Unexpected response code")
  
    #get metadata
    res = @client.get('/metadata.json')
  
    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)
	
	assert_equal(5, body.size)
	assert_equal(8, body['word_count'])
	assert_equal(4, body['min_length'])
	assert_equal(5, body['max_length'])
	assert_equal('5', body['median_length'])
	assert_equal('4.625', body['avg_length'])
  end
  
  def test_metadata_dictionary    
	 
	# refresh dataset
	res = @client.get('/reload.json')
    assert_equal('200', res.code, "Unexpected response code")
	  
	#get metadata
    res = @client.get('/metadata.json')
  
    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)
	
	#dev note: although there are 235886 entries in the dictionary, 
	#two words include hyphens which violates the provided definition of a word as only consisting of the 26 roman characters.
	assert_equal(5, body.size)
	assert_equal(235884, body['word_count'])
	assert_equal(1, body['min_length'])
	assert_equal(24, body['max_length'])
	assert_equal('9', body['median_length'])
	assert_equal('9.569', body['avg_length'])
  end
  
  def test_metadata_empty_dictionary    
	
	# delete dictionary
	res = @client.delete('/words.json')

    assert_equal('204', res.code, "Unexpected response code")
	
	#get metadata
    res = @client.get('/metadata.json')
  
    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)
	
	assert_equal(5, body.size)
	assert_equal(0, body['word_count'])
	assert_equal(0, body['min_length'])
	assert_equal(0, body['max_length'])
	assert_equal(0, body['median_length'])
	assert_equal(0, body['avg_length'])
	
  end
  
  def test_largest_anagram_set_manual
	
	#add more words to dictionary
    res = @client.post('/words.json', nil, {"words" => ["least", "slate", "stale", "steal", "tales"] })
    assert_equal('201', res.code, "Unexpected response code")
  
    #get anagram set
    res = @client.get('/most.json')
  
    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)
	
	assert_equal(1, body['anagrams'].size)
	assert_equal(5, body['anagrams'][0].size)
	expected_anagrams = %w(least slate stale steal tales)
    assert_equal(expected_anagrams, body['anagrams'][0].sort)	
  end
  
  def test_largest_anagram_set_tie_manual	
	
	#add more words to dictionary
    res = @client.post('/words.json', nil, {"words" => ["slate", "stale", "steal"] })
    assert_equal('201', res.code, "Unexpected response code")
  
    #get anagram set
    res = @client.get('/most.json')
  
    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)
	
	assert_equal(2, body['anagrams'].size)
	assert_equal(3, body['anagrams'][0].size)
	assert_equal(3, body['anagrams'][1].size)
	expected_anagrams = %w(dare dear read)
    assert_equal(expected_anagrams, body['anagrams'][0].sort)		
	expected_anagrams = %w(slate stale steal)
    assert_equal(expected_anagrams, body['anagrams'][1].sort)	
  end
  
  def test_largest_anagram_set_dictionary
	# refresh dataset
	res = @client.get('/reload.json')
    assert_equal('200', res.code, "Unexpected response code")
	
	#get anagram set
    res = @client.get('/most.json')
  
    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)
	
	assert_equal(3, body['anagrams'].size)
	assert_equal(9, body['anagrams'][0].size)
	assert_equal(9, body['anagrams'][1].size)
	assert_equal(9, body['anagrams'][2].size)
	expected_anagrams = %w(caret carte cater crate creat creta react recta trace)
    assert_equal(expected_anagrams, body['anagrams'][0].sort)		
	expected_anagrams = %w(ester estre reest reset steer stere stree terse tsere)
    assert_equal(expected_anagrams, body['anagrams'][1].sort)	
	expected_anagrams = %w(angor argon goran grano groan nagor orang organ rogan)
    assert_equal(expected_anagrams, body['anagrams'][2].sort)		
  end
  
  def test_largest_anagram_set_empty_dictionary
	# delete dictionary
	res = @client.delete('/words.json')

    assert_equal('204', res.code, "Unexpected response code")
	
	#get anagram set
    res = @client.get('/most.json')
  
    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)		
	
	assert_equal(0, body['anagrams'].size)
  end
  
  def test_retrieve_anagram_group_size_zero
	
	# refresh dataset
	res = @client.get('/reload.json')
    assert_equal('200', res.code, "Unexpected response code")
	
	#get anagram set
    res = @client.get('/groups.json','size=-0')
  
    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)		
	
	assert_equal(220674, body['anagrams'].size)
  end
  
  def test_retrieve_anagram_group_size_negative
	
	# refresh dataset
	res = @client.get('/reload.json')
    assert_equal('200', res.code, "Unexpected response code")
	
	#get anagram set
    res = @client.get('/groups.json','size=-1')
  
    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)		
	
	assert_equal(220674, body['anagrams'].size)
  end
  
  def test_retrieve_anagram_group_size_one	
	
	# refresh dataset
	res = @client.get('/reload.json')
    assert_equal('200', res.code, "Unexpected response code")
	
	#get anagram set
    res = @client.get('/groups.json','size=1')
  
    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)		
	
	assert_equal(220674, body['anagrams'].size)
  end
  
  def test_retrieve_anagram_group_size_five	
	
	# refresh dataset
	res = @client.get('/reload.json')
    assert_equal('200', res.code, "Unexpected response code")
	
	#get anagram set
    res = @client.get('/groups.json','size=5')
  
    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)		
	
	assert_equal(171, body['anagrams'].size)
  end
  
  def test_retrieve_anagram_group_size_twenty_five
	
	# refresh dataset
	res = @client.get('/reload.json')
    assert_equal('200', res.code, "Unexpected response code")
	
	#get anagram set
    res = @client.get('/groups.json','size=25')
  
    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)		
	
	assert_equal(0, body['anagrams'].size)
  end
  
  def test_retrieve_anagram_group_size_invalid
	
	# refresh dataset
	res = @client.get('/reload.json')
    assert_equal('200', res.code, "Unexpected response code")
	
	#get anagram set
    res = @client.get('/groups.json','size=size')
  
    assert_equal('400', res.code, "Unexpected response code")
  end
  
  def test_delete_anagrams
    
	#delete the anagrams	
	res = @client.delete('/anagrams/read.json')

    assert_equal('204', res.code, "Unexpected response code")

    # should fetch an empty body
    res = @client.get('/anagrams/read.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
	
	# double check with one of the other anagrams
	res = @client.get('/anagrams/dear.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
  end
  
  def test_delete_anagrams_of_non_present_word
    
	#delete the anagrams	
	res = @client.delete('/anagrams/daer.json')

    assert_equal('204', res.code, "Unexpected response code")

    # should fetch an empty body
    res = @client.get('/anagrams/daer.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
	
	# double check with one of the other anagrams
	res = @client.get('/anagrams/read.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
  end
  
  def test_delete_anagrams_multiple_times	
	
	#delete the anagrams
	3.times do
      res = @client.delete('/anagrams/read.json')

      assert_equal('204', res.code, "Unexpected response code")
    end
	
	# should fetch an empty body
    res = @client.get('/anagrams/read.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
	
	# double check with one of the other anagrams
	res = @client.get('/anagrams/dear.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
  end  
  
  def test_delete_anagrams_dictionary	
    
	# refresh dataset
	res = @client.get('/reload.json')
    assert_equal('200', res.code, "Unexpected response code")	
	
	#delete the anagrams
    res = @client.delete('/anagrams/stale.json')

    assert_equal('204', res.code, "Unexpected response code")

    # should fetch an empty body
    res = @client.get('/anagrams/stale.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
	
	# double check with one of the other anagrams
	res = @client.get('/anagrams/steal.json')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_equal(0, body['anagrams'].size)
  end
  
  def test_case_insensitive_param_true
	
	#add proper nouns to the dictionary
	res = @client.post('/words.json', nil, {"words" => ["Read", "Dear", "Dare"] })
	
	#check if any anagrams are returned with flag
	res = @client.get('/anagrams/dear.json','caseinsensitive=true')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(Dare Dear Read dare read)
    assert_equal(expected_anagrams, body['anagrams'].sort)
	
  end
  
  def test_case_insensitive_param_dictionary
	
	# refresh dataset
	res = @client.get('/reload.json')
    assert_equal('200', res.code, "Unexpected response code")
	
	#check if any anagrams are returned with flag
	res = @client.get('/anagrams/baby.json','caseinsensitive=true')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(Abby)
    assert_equal(expected_anagrams, body['anagrams'].sort)
	
	
  end
  
  def test_case_insensitive_param_no_proper_nouns
	
	# fetch anagrams
    res = @client.get('/anagrams/read.json','caseinsensitive=true')

    assert_equal('200', res.code, "Unexpected response code")
    assert_not_nil(res.body)

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(dare dear)
    assert_equal(expected_anagrams, body['anagrams'].sort)
	
	# fetch a different anagram in the same set
    res = @client.get('/anagrams/dare.json','caseinsensitive=true')

    assert_equal('200', res.code, "Unexpected response code")
    assert_not_nil(res.body)

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(dear read)
    assert_equal(expected_anagrams, body['anagrams'].sort)
  end
  
  def test_case_insensitive_param_false
	
	#add proper nouns to the dictionary
	res = @client.post('/words.json', nil, {"words" => ["Read", "Dear", "Dare"] })
	
	#check if any anagrams are returned with flag
	res = @client.get('/anagrams/dear.json','caseinsensitive=false')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(dare read)
    assert_equal(expected_anagrams, body['anagrams'].sort)
  end

  def test_case_insensitive_param_invalid  
	
	#add proper nouns to the dictionary
	res = @client.post('/words.json', nil, {"words" => ["Read", "Dear", "Dare"] })
	
	#check if any anagrams are returned with flag
	res = @client.get('/anagrams/dear.json','caseinsensitive=xxyyxx')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(dare read)
    assert_equal(expected_anagrams, body['anagrams'].sort)
  end
  
  def test_case_insensitive_true_param_with_limit_one_param
    
	#add proper nouns to the dictionary
	res = @client.post('/words.json', nil, {"words" => ["Read", "Dear", "Dare"] })
	
	#check if any anagrams are returned with flag
	res = @client.get('/anagrams/dear.json','caseinsensitive=true&limit=1')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(Dare)
    assert_equal(expected_anagrams, body['anagrams'].sort)
  end
  
  def test_case_insensitive_false_param_with_limit_one_param
    
	#add proper nouns to the dictionary
	res = @client.post('/words.json', nil, {"words" => ["Read", "Dear", "Dare"] })
	
	#check if any anagrams are returned with flag
	res = @client.get('/anagrams/dear.json','caseinsensitive=false&limit=1')

    assert_equal('200', res.code, "Unexpected response code")

    body = JSON.parse(res.body)

    assert_not_nil(body['anagrams'])

    expected_anagrams = %w(read)
    assert_equal(expected_anagrams, body['anagrams'].sort)
  end  
  
end