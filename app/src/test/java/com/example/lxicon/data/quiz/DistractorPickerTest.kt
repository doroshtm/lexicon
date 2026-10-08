package com.example.lxicon.data.quiz

import com.example.lxicon.data.ReviewState
import com.example.lxicon.data.dictionary.Entry
import com.example.lxicon.data.dictionary.Example
import com.example.lxicon.data.dictionary.Sense
import com.example.lxicon.data.dictionary.Synonym
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DistractorPickerTest {
    private fun entry(id: Int, word: String, cls: String="Adjetivo"): Entry {
        val key = textKey(word);
        return Entry(id, word, cls, key, key.reversed(), 0);
    }

    private fun sense(id: String, entryId: Int, definition: String, type: String="sentido", order: Int=1): Sense {
        return Sense(id, entryId, order, type, definition, null, 0);
    }

    private fun content(word: String, entries: List<Entry>, senses: List<Sense>, synonyms: List<Synonym> = emptyList(), phrases: List<CardPhrase> = emptyList()): CardContent {
        return CardContent(QuizCard(textKey(word), word, phrases), entries, senses, synonyms, emptyList());
    }

    private fun candidate(entry: Entry, senses: List<Sense>, synonymWords: List<String> = emptyList()): DistractorCandidate {
        return DistractorCandidate(entry, senses, synonymWords, 0);
    }

    private val prolixo = entry(1, "prolixo");
    private val prolixoSense = sense("s1", 1, "que se alonga demais no falar ou escrever");
    private val target = content("prolixo", listOf(prolixo), listOf(prolixoSense));

    @Test
    fun vetoesTheSameWordAndItsSynonyms() {
        val synTarget = content("prolixo", listOf(prolixo), listOf(prolixoSense, sense("s2", 1, "extenso", "sinonimos", 2)), listOf(Synonym("s2", 1, "verboso")));
        assertTrue(entryVetoed(candidate(entry(2, "prolixo", "Substantivo"), emptyList()), prolixo, target, emptySet()));
        assertTrue(entryVetoed(candidate(entry(3, "verboso"), listOf(sense("s3", 3, "cheio de palavras"))), prolixo, synTarget, emptySet()));
    }

    @Test
    fun vetoesCandidateThatListsTheTargetAsSynonym() {
        val candidate = candidate(entry(4, "palavroso"), listOf(sense("s4", 4, "x", "sinonimos")), listOf("prolixo"));
        assertTrue(entryVetoed(candidate, prolixo, target, emptySet()));
    }

    @Test
    fun vetoesIdenticalDefinitionsAndSpellingVariants() {
        assertTrue(entryVetoed(candidate(entry(5, "extenso"), listOf(sense("s5", 5, "Que se alonga demais no falar ou escrever!"))), prolixo, target, emptySet()));
        val projetil = entry(6, "projetil");
        val projectil = candidate(entry(7, "projectil"), listOf(sense("s7", 7, "algo lançado")));
        assertTrue(entryVetoed(projectil, projetil, content("projetil", listOf(projetil), listOf(sense("s6", 6, "outro texto"))), emptySet()));
    }

    @Test
    fun acceptsAnUnrelatedCandidate() {
        assertFalse(entryVetoed(candidate(entry(8, "capitalino"), listOf(sense("s8", 8, "relativo à capital"))), prolixo, target, emptySet()));
    }

    @Test
    fun extraVetoKeysApply() {
        assertTrue(entryVetoed(candidate(entry(9, "ser"), listOf(sense("s9", 9, "existir"))), prolixo, target, setOf("ser")));
    }

    @Test
    fun chooseOptionsNeedsThreeDistinctCandidates() {
        val random = Random(1);
        assertNull(chooseOptions(listOf(Pair(1, "a b"), Pair(1, "A  B"), Pair(1, "c")), "correta", random));
        val options = chooseOptions(listOf(Pair(3, "um"), Pair(2, "dois"), Pair(1, "três"), Pair(1, "correta")), "correta", random);
        assertNotNull(options);
        assertEquals(3, options!!.size);
        assertFalse(options.contains("correta"));
    }

    @Test
    fun assembleOptionsPlacesTheCorrectAnswerEverywhere() {
        val random = Random(42);
        val positions = IntArray(4);
        repeat(1000) {
            val (options, correctIndex) = assembleOptions("certa", listOf("a", "b", "c"), random);
            assertEquals(4, options.size);
            assertEquals("certa", options[correctIndex]);
            ++positions[correctIndex];
        }
        positions.forEach { assertTrue(it in 180..320) }
    }

    @Test
    fun givesAwayDetectsTheWordAndItsStem() {
        assertTrue(givesAway("ato de prolixar algo", "prolixo"));
        assertTrue(givesAway("que fala de modo prolixo", "prolixo"));
        assertFalse(givesAway("que se alonga demais", "prolixo"));
    }

    @Test
    fun pickSensePrefersLinkedSenseOnEvenTurns() {
        val senses = listOf(sense("a", 1, "primeiro"), sense("b", 1, "segundo", order=2), sense("c", 1, "terceiro", order=3));
        val linked = content("x", listOf(prolixo), senses, phrases=listOf(CardPhrase("frase", "c")));
        assertEquals("c", pickSense(linked, 0)!!.id);
        assertEquals("b", pickSense(linked, 1)!!.id);
        assertEquals("c", pickSense(linked, 2)!!.id);
    }

    @Test
    fun synonymOnlyWordsGetSynonymQuestions() {
        val onlySynonyms = content("disparate", listOf(entry(10, "disparate", "Substantivo")), listOf(sense("d1", 10, "x", "sinonimos")), listOf(Synonym("d1", 1, "tolice")));
        assertEquals(QuestionType.SYNONYMS, typeOrder(onlySynonyms, ReviewState("disparate"))[0]);
        assertNull(pickSense(onlySynonyms, 0));
    }

    @Test
    fun exampleSpanUsesHighlightWhenItMatches() {
        val example = Example("s1", 1, "um velho caquético", 9, 18);
        val span = exampleSpan(example, "caquético", emptyMap());
        assertEquals("caquético", "um velho caquético".substring(span!!.first, span.last + 1));
        val inflected = Example("s1", 2, "ideias caquéticas", 7, 17);
        assertNotNull(exampleSpan(inflected, "caquético", emptyMap()));
    }

    @Test
    fun exampleSpanRejectsLongOrUnrelatedText() {
        assertNull(exampleSpan(Example("s1", 1, "uma frase sem a palavra", null, null), "caquético", emptyMap()));
        assertNull(exampleSpan(Example("s1", 2, "caquético ".repeat(30), null, null), "caquético", emptyMap()));
    }
}
