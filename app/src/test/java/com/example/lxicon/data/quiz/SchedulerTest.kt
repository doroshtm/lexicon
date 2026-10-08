package com.example.lxicon.data.quiz

import com.example.lxicon.data.ReviewState
import com.example.lxicon.data.StoredWord
import org.junit.Assert.assertEquals
import org.junit.Test

class SchedulerTest {
    @Test
    fun buildCardsGroupsSameWordAndKeepsPhrases() {
        val cards = buildCards(listOf(
            StoredWord("Prolixo", "Um discurso prolixo", "s1", 1),
            StoredWord("casa", null, null, 2),
            StoredWord("prolixo", "Texto prolixo demais", "s2", 3)
        ));
        assertEquals(2, cards.size);
        assertEquals("prolixo", cards[0].key);
        assertEquals(2, cards[0].phrases.size);
        assertEquals(0, cards[1].phrases.size);
    }

    @Test
    fun correctAnswerMovesUpAndCapsAtLastBox() {
        var state = ReviewState("a");
        state = afterAnswer(state, true, 100, false);
        assertEquals(1, state.box);
        assertEquals(101L, state.dueDay);
        repeat(10) { state = afterAnswer(state, true, 100, false); }
        assertEquals(5, state.box);
        assertEquals(130L, state.dueDay);
        assertEquals(11, state.timesAsked);
    }

    @Test
    fun wrongAnswerResetsToBoxZeroDueToday() {
        val state = afterAnswer(ReviewState("a", 4, 120, 6), false, 100, false);
        assertEquals(0, state.box);
        assertEquals(100L, state.dueDay);
        assertEquals(7, state.timesAsked);
    }

    @Test
    fun practiceCorrectDoesNotMoveBox() {
        val state = afterAnswer(ReviewState("a", 2, 110, 3), true, 100, true);
        assertEquals(2, state.box);
        assertEquals(110L, state.dueDay);
        assertEquals(4, state.timesAsked);
    }

    @Test
    fun sessionOrderPutsOverdueBeforeNewAndSkipsNotDue() {
        val cards = listOf(QuizCard("new", "new", emptyList()), QuizCard("late", "late", emptyList()), QuizCard("early", "early", emptyList()), QuizCard("future", "future", emptyList()));
        val states = mapOf(
            "late" to ReviewState("late", 1, 90, 1),
            "early" to ReviewState("early", 1, 80, 1),
            "future" to ReviewState("future", 2, 200, 1)
        );
        assertEquals(listOf("early", "late", "new"), sessionOrder(cards, states, 100, false).map { it.key });
        assertEquals(3, dueCount(cards, states, 100));
        assertEquals(listOf("new", "early", "late", "future"), sessionOrder(cards, states, 100, true).map { it.key });
    }
}
