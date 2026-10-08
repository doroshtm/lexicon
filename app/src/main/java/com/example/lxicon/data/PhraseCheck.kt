package com.example.lxicon.data

import com.example.lxicon.data.dictionary.DictionaryDatabase

fun phraseContainsWord(phrase: String, word: String): Boolean {
    val wordKey = DictionaryDatabase.searchKey(word);
    if(wordKey.isEmpty()) {
        return false;
    }
    val wholeWord = Regex("(?<![\\p{L}\\p{N}])" + Regex.escape(wordKey) + "(?![\\p{L}\\p{N}])");
    return wholeWord.containsMatchIn(DictionaryDatabase.searchKey(phrase));
}
