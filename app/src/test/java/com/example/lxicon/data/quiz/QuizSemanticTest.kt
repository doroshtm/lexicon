package com.example.lxicon.data.quiz

import com.example.lxicon.data.ReviewState
import com.example.lxicon.data.dictionary.Entry
import com.example.lxicon.data.dictionary.Sense
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizSemanticTest {
    private fun entry(id: Int, word: String): Entry {
        val key = textKey(word);
        return Entry(id, word, "Adjetivo", key, key.reversed(), 0);
    }

    private val related = listOf(
        Triple("esbanjador", "que gasta dinheiro de modo excessivo", setOf("gasta", "dinhei", "excess")),
        Triple("mesquinho", "que se apega a ninharias e ao dinheiro", setOf("apega", "ninhar", "dinhei")),
        Triple("perdulario", "que gasta sem medida o dinheiro que tem", setOf("gasta", "medida", "dinhei")),
        Triple("sovina", "apego exagerado ao dinheiro", setOf("apego", "exager", "dinhei")),
        Triple("gastador", "que gasta muito dinheiro", setOf("gasta", "dinhei"))
    );
    private val unrelated = listOf("verde", "ligeiro", "salino", "capitular", "fosco", "rouco", "tenro", "agudo");

    private val entries = listOf(entry(1, "avarento")) + related.mapIndexed { index, item -> entry(10 + index, item.first) } + unrelated.mapIndexed { index, word -> entry(30 + index, word) };

    private val senses = listOf(Sense("a1", 1, 1, "sentido", "que tem apego excessivo ao dinheiro", null, 0)) +
        related.mapIndexed { index, item -> Sense("r" + index, 10 + index, 1, "sentido", item.second, null, 0) } +
        unrelated.mapIndexed { index, word -> Sense("u" + index, 30 + index, 1, "sentido", "definição qualquer sobre " + word.reversed() + " sem relação", null, 0) };

    private val termIndex = mapOf("a1" to setOf("apego", "excess", "dinhei")) + related.mapIndexed { index, item -> "r" + index to item.third };

    private val dao = FakeDictionaryDao(entries, senses, termIndex = termIndex);

    @Test
    fun meaningDistractorsComeFromRelatedSensesWhenTheyExist() = runBlocking {
        val relatedDefinitions = related.map { it.second }.toSet();
        var relatedCount = 0;
        for(seed in 1..30) {
            val generator = QuizGenerator(dao, Random(seed));
            val content = generator.load(QuizCard("avarento", "avarento", emptyList()))!!;
            val question = generator.generate(content, ReviewState("avarento", 0, 0, 0))!!;
            assertEquals(QuestionType.MEANING, question.type);
            val distractors = question.options.filterIndexed { index, _ -> index != question.correctIndex };
            val fromRelated = distractors.count { relatedDefinitions.contains(it) };
            assertTrue(fromRelated >= 2);
            relatedCount += fromRelated;
        }
        assertTrue(relatedCount >= 60);
    }

    @Test
    fun relatedSenseThatSharesEveryTermIsSkipped() = runBlocking {
        val onlyDuplicate = FakeDictionaryDao(entries, senses + Sense("x1", 99, 1, "sentido", "que tem apego excessivo ao dinheiro quase igual", null, 0), termIndex = termIndex + ("x1" to setOf("apego", "excess", "dinhei")));
        val generator = QuizGenerator(onlyDuplicate, Random(1));
        val content = generator.load(QuizCard("avarento", "avarento", emptyList()))!!;
        val question = generator.generate(content, ReviewState("avarento", 0, 0, 0))!!;
        assertTrue(question.options.none { it.contains("quase igual") });
    }
}
