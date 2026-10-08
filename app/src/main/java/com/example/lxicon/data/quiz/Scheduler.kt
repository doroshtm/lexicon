package com.example.lxicon.data.quiz

import com.example.lxicon.data.ReviewState
import com.example.lxicon.data.StoredWord

val reviewIntervals = listOf(0, 1, 3, 7, 14, 30);

fun buildCards(words: List<StoredWord>): List<QuizCard> {
    val groups = LinkedHashMap<String, MutableList<StoredWord>>();
    words.forEach {
        groups.getOrPut(textKey(it.word)) { mutableListOf() }.add(it);
    }
    return groups.map { group ->
        val phrases = group.value.filter { it.phrase != null }.map { CardPhrase(it.phrase!!, it.senseId) };
        QuizCard(group.key, group.value.first().word, phrases)
    };
}

fun isDue(state: ReviewState?, today: Long): Boolean {
    return state == null || state.dueDay <= today;
}

fun dueCount(cards: List<QuizCard>, states: Map<String, ReviewState>, today: Long): Int {
    return cards.count { isDue(states[it.key], today) };
}

fun sessionOrder(cards: List<QuizCard>, states: Map<String, ReviewState>, today: Long, practice: Boolean): List<QuizCard> {
    if(practice) {
        return cards.sortedBy { states[it.key]?.dueDay ?: 0L };
    }
    val due = cards.filter { isDue(states[it.key], today) };
    val seen = due.filter { states[it.key] != null }.sortedWith(compareBy({ states[it.key]!!.dueDay }, { it.key }));
    val fresh = due.filter { states[it.key] == null };
    return seen + fresh;
}

fun afterAnswer(state: ReviewState, correct: Boolean, today: Long, practice: Boolean): ReviewState {
    if(!correct) {
        return state.copy(box = 0, dueDay = today, timesAsked = state.timesAsked + 1);
    }
    if(practice) {
        return state.copy(timesAsked = state.timesAsked + 1);
    }
    val box = minOf(state.box + 1, reviewIntervals.size - 1);
    return state.copy(box = box, dueDay = today + reviewIntervals[box], timesAsked = state.timesAsked + 1);
}
