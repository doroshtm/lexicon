package com.example.lxicon.data.quiz

import com.example.lxicon.data.ReviewState
import com.example.lxicon.data.dictionary.Entry
import com.example.lxicon.data.dictionary.Example
import com.example.lxicon.data.dictionary.Sense
import com.example.lxicon.data.dictionary.Synonym
import com.example.lxicon.data.dictionary.WordForm
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizGeneratorTest {
    private val adjectives = listOf("prolixo", "capitalista", "capitalino", "capitular", "capitoso", "cristalino", "salino", "alcalino", "argentino", "verboso", "extenso", "conciso", "pontual", "sagaz", "ranzinza", "tenaz", "voraz");

    private fun entry(id: Int, word: String, cls: String): Entry {
        val key = textKey(word);
        return Entry(id, word, cls, key, key.reversed(), 0);
    }

    private val entries = adjectives.mapIndexed { index, word -> entry(index + 1, word, "Adjetivo") } + entry(100, "disparate", "Substantivo") + entry(101, "tolice", "Substantivo") + entry(102, "bobagem", "Substantivo") + entry(103, "asneira", "Substantivo") + entry(104, "mentira", "Substantivo") + entry(105, "nonsense", "Substantivo") + entry(106, "baboseira", "Substantivo") + entry(107, "patacoada", "Substantivo") + entry(108, "absurdez", "Substantivo");

    private val senses = adjectives.mapIndexed { index, word -> Sense("s" + (index + 1), index + 1, 1, "sentido", "definição distinta número " + (index + 1) + " para " + word.reversed(), null, 0) } + listOf(
        Sense("d1", 100, 1, "sinonimos", "tolice", null, 0),
        Sense("t1", 101, 1, "sentido", "dito sem sentido ou juízo algum", null, 0),
        Sense("b1", 102, 1, "sinonimos", "besteira", null, 0),
        Sense("a1", 103, 1, "sinonimos", "tolice", null, 0),
        Sense("m1", 104, 1, "sinonimos", "falsidade", null, 0),
        Sense("n1", 105, 1, "sinonimos", "x", null, 0),
        Sense("n2", 106, 1, "sinonimos", "x", null, 0),
        Sense("n3", 107, 1, "sinonimos", "x", null, 0),
        Sense("n4", 108, 1, "sinonimos", "x", null, 0)
    );

    private val synonyms = listOf(Synonym("d1", 1, "tolice"), Synonym("d1", 2, "absurdo"), Synonym("b1", 1, "besteira"), Synonym("a1", 1, "tolice"), Synonym("m1", 1, "falsidade"), Synonym("n1", 1, "disparate2"), Synonym("n1", 2, "pateta"), Synonym("n2", 1, "bobo"), Synonym("n2", 2, "tonto"), Synonym("n3", 1, "lorota"), Synonym("n3", 2, "peta"), Synonym("n4", 1, "exagero"), Synonym("n4", 2, "demasia"));

    private val dao = FakeDictionaryDao(entries, senses, synonyms, listOf(Example("s1", 1, "um discurso prolixo e cansativo", 11, 18)), listOf(WordForm("prolixos", "prolixos", "prolixo", "forms")));

    private fun generator(seed: Int) = QuizGenerator(dao, Random(seed));

    private fun card(word: String, phrases: List<CardPhrase> = emptyList()) = QuizCard(textKey(word), word, phrases);

    private fun assertValid(question: QuizQuestion) {
        assertEquals(4, question.options.size);
        assertEquals(4, question.options.map { normDef(it) }.toSet().size);
        assertTrue(question.correctIndex in 0..3);
    }

    @Test
    fun meaningQuestionHasFourDistinctOptionsWithTheRightAnswer() = runBlocking {
        for(seed in 1..20) {
            val generator = generator(seed);
            val content = generator.load(card("prolixo"))!!;
            val question = generator.generate(content, ReviewState("prolixo", 0, 0, 0))!!;
            assertEquals(QuestionType.MEANING, question.type);
            assertValid(question);
            assertEquals("definição distinta número 1 para oxilorp", question.options[question.correctIndex]);
        }
    }

    @Test
    fun clozeUsesTheUsersSentenceAndBlanksTheWord() = runBlocking {
        val generator = generator(3);
        val content = generator.load(card("prolixo", listOf(CardPhrase("O discurso prolixo cansou a plateia", "s1"))))!!;
        val question = generator.generate(content, ReviewState("prolixo", 2, 0, 0))!!;
        assertEquals(QuestionType.CLOZE, question.type);
        assertEquals("O discurso _____ cansou a plateia", question.context);
        assertValid(question);
        assertEquals("prolixo", question.options[question.correctIndex]);
    }

    @Test
    fun clozeFallsBackToADictionaryExample() = runBlocking {
        val generator = generator(4);
        val content = generator.load(card("prolixo"))!!;
        val question = generator.generate(content, ReviewState("prolixo", 2, 0, 0))!!;
        assertEquals(QuestionType.CLOZE, question.type);
        assertEquals("um discurso _____ e cansativo", question.context);
    }

    @Test
    fun synonymOnlyWordGetsSynonymQuestionWithoutOverlap() = runBlocking {
        val generator = generator(5);
        val content = generator.load(card("disparate", emptyList()))!!;
        val question = generator.generate(content, ReviewState("disparate", 0, 0, 0))!!;
        assertEquals(QuestionType.SYNONYMS, question.type);
        assertValid(question);
        question.options.forEachIndexed { index, option ->
            if(index != question.correctIndex) {
                assertFalse(option.contains("tolice") || option.contains("absurdo"));
            }
        }
    }

    @Test
    fun unknownWordCannotBeLoaded() = runBlocking {
        assertNull(generator(6).load(card("xyzzy")));
    }

    @Test
    fun inflectedWordResolvesThroughItsLemma() = runBlocking {
        val content = generator(7).load(card("prolixos"));
        assertNotNull(content);
        assertEquals("prolixo", content!!.entries.first().word);
    }
}
