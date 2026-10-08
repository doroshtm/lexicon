package com.example.lxicon.data.dictionary

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DictionaryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao by lazy { DictionaryDatabase.get(application).dao() };

    val results = mutableStateListOf<EntryResult>();
    var notFound by mutableStateOf(false);

    fun clear() {
        results.clear();
        notFound = false;
    }

    fun search(text: String, onDone: (Boolean) -> Unit = {}) {
        if(text.isBlank()) {
            onDone(false);
            return;
        }
        viewModelScope.launch {
            val entries = resolveEntries(dao, text);
            val sensesByEntry = dao.sensesOf(entries.map { it.id }).groupBy { it.entryId };
            results.clear();
            results.addAll(entries.map { EntryResult(it, sensesByEntry[it.id] ?: emptyList()) });
            notFound = results.isEmpty();
            Log.d("DictionaryViewModel", "search '" + text + "': " + results.size + " entries");
            onDone(results.isNotEmpty());
        };
    }
}
