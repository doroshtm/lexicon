package com.example.lxicon.data.quiz

import com.example.lxicon.data.dictionary.DictionaryDao
import com.example.lxicon.data.dictionary.Entry
import com.example.lxicon.data.dictionary.Example
import com.example.lxicon.data.dictionary.SenseMatch
import com.example.lxicon.data.dictionary.Sense
import com.example.lxicon.data.dictionary.Synonym
import com.example.lxicon.data.dictionary.WordForm

class FakeDictionaryDao(
    private val entries: List<Entry>,
    private val senses: List<Sense>,
    private val synonyms: List<Synonym> = emptyList(),
    private val examples: List<Example> = emptyList(),
    private val forms: List<WordForm> = emptyList(),
    private val termIndex: Map<String, Set<String>> = emptyMap()
) : DictionaryDao {
    override suspend fun termsOf(senseId: String): List<String> = (termIndex[senseId] ?: emptySet()).toList().sorted();

    override suspend fun similarSenses(terms: List<String>, partOfSpeech: String, entryId: Int, limit: Int): List<SenseMatch> {
        val classOf = entries.associate { it.id to it };
        return senses.filter { sense ->
            val entry = classOf[sense.entryId];
            sense.type == "sentido" && sense.noise == 0 && sense.entryId != entryId && entry != null && entry.partOfSpeech == partOfSpeech && entry.noise == 0
        }.map { SenseMatch(it.id, (termIndex[it.id] ?: emptySet()).count { term -> terms.contains(term) }) }
            .filter { it.shared > 0 }.sortedByDescending { it.shared }.take(limit);
    }

    override suspend fun entriesByKey(key: String): List<Entry> = entries.filter { it.key == key }.sortedBy { it.id };

    override suspend fun sensesOf(entryIds: List<Int>): List<Sense> = senses.filter { entryIds.contains(it.entryId) }.sortedWith(compareBy({ it.entryId }, { it.order }));

    override suspend fun lemmasByKey(key: String): List<String> = forms.filter { it.key == key }.map { it.lemma }.sorted();

    override suspend fun sensesByIds(senseIds: List<String>): List<Sense> = senses.filter { senseIds.contains(it.id) };

    override suspend fun entriesByIds(entryIds: List<Int>): List<Entry> = entries.filter { entryIds.contains(it.id) };

    override suspend fun suffixNeighborsAfter(partOfSpeech: String, reversedKey: String, limit: Int): List<Entry> =
        entries.filter { it.partOfSpeech == partOfSpeech && it.reversedKey > reversedKey && it.noise == 0 }.sortedBy { it.reversedKey }.take(limit);

    override suspend fun suffixNeighborsBefore(partOfSpeech: String, reversedKey: String, limit: Int): List<Entry> =
        entries.filter { it.partOfSpeech == partOfSpeech && it.reversedKey < reversedKey && it.noise == 0 }.sortedByDescending { it.reversedKey }.take(limit);

    override suspend fun prefixNeighborsAfter(key: String, upper: String, partOfSpeech: String, limit: Int): List<Entry> =
        entries.filter { it.key > key && it.key < upper && it.partOfSpeech == partOfSpeech && it.noise == 0 }.sortedBy { it.key }.take(limit);

    override suspend fun prefixNeighborsBefore(key: String, lower: String, partOfSpeech: String, limit: Int): List<Entry> =
        entries.filter { it.key < key && it.key >= lower && it.partOfSpeech == partOfSpeech && it.noise == 0 }.sortedByDescending { it.key }.take(limit);

    override suspend fun synonymsOf(senseIds: List<String>): List<Synonym> = synonyms.filter { senseIds.contains(it.senseId) }.sortedWith(compareBy({ it.senseId }, { it.order }));

    override suspend fun examplesOf(senseIds: List<String>): List<Example> = examples.filter { senseIds.contains(it.senseId) }.sortedWith(compareBy({ it.senseId }, { it.order }));

    override suspend fun formsByKeys(keys: List<String>): List<WordForm> = forms.filter { keys.contains(it.key) };
}
