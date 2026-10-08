package com.example.lxicon.data

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lxicon.data.dictionary.DictionaryDatabase
import kotlinx.coroutines.launch

class PalavrasViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = UserDatabase.get(application).storedWordDao();

    private val dictionaryDao by lazy { DictionaryDatabase.get(application).dao() };

    val words = mutableStateListOf<StoredWord>();
    val senseDefinitions = mutableStateMapOf<String, String>();

    init {
        viewModelScope.launch {
            words.addAll(dao.all());
            loadDefinitions();
        };
    }

    private suspend fun loadDefinitions() {
        val missing = words.mapNotNull { it.senseId }.distinct().filter { !senseDefinitions.containsKey(it) };
        missing.chunked(500).forEach { chunk ->
            dictionaryDao.sensesByIds(chunk).forEach { senseDefinitions[it.id] = it.definition };
        }
    }

    fun save(word: StoredWord): SaveResult {
        if(word.senseId == null) {
            if(words.any { it.word == word.word && it.phrase == word.phrase && it.senseId == null }) {
                return SaveResult.DUPLICATE;
            }
        } else {
            val wordKey = DictionaryDatabase.searchKey(word.word);
            if(words.any { it.senseId == word.senseId && it.phrase != null && DictionaryDatabase.searchKey(it.word) == wordKey }) {
                return SaveResult.SENSE_TAKEN;
            }
        }
        viewModelScope.launch {
            val id = dao.insert(word);
            words.add(word.copy(id = id));
            loadDefinitions();
        };
        return SaveResult.SAVED;
    }
    fun update(before: StoredWord, after: StoredWord) {
        viewModelScope.launch {
            val beforePos = words.indexOf(before);
            if(beforePos == -1) {
                return@launch;
            }
            val sameWord = DictionaryDatabase.searchKey(before.word) == DictionaryDatabase.searchKey(after.word);
            val updated = after.copy(id = before.id, senseId = if(sameWord) {before.senseId} else {null});
            dao.update(updated);
            words[beforePos] = updated;
        };
    }
    fun delete(word: StoredWord) {
        viewModelScope.launch {
            dao.delete(word);
            words.remove(word);
        };
    }

}
