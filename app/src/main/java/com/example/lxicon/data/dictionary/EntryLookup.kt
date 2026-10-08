package com.example.lxicon.data.dictionary

suspend fun resolveEntries(dao: DictionaryDao, text: String): List<Entry> {
    val key = DictionaryDatabase.searchKey(text);
    val entries = dao.entriesByKey(key);
    if(entries.isNotEmpty()) {
        return entries;
    }
    val lemmas = dao.lemmasByKey(key).distinct();
    return lemmas.flatMap { dao.entriesByKey(DictionaryDatabase.searchKey(it)) }.distinctBy { it.id };
}
