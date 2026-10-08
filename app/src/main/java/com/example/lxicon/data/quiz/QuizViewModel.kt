package com.example.lxicon.data.quiz

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lxicon.data.ReviewState
import com.example.lxicon.data.UserDatabase
import com.example.lxicon.data.dictionary.DictionaryDatabase
import java.time.LocalDate
import kotlin.random.Random
import kotlinx.coroutines.launch

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val wordDao = UserDatabase.get(application).storedWordDao();
    private val reviewDao = UserDatabase.get(application).reviewStateDao();
    private val generator by lazy { QuizGenerator(DictionaryDatabase.get(application).dao(), Random.Default) };
    private var states = HashMap<String, ReviewState>();
    private var cards = emptyList<QuizCard>();

    var phase by mutableStateOf(QuizPhase.LOADING);
    var dueCount by mutableStateOf(0);
    var totalCount by mutableStateOf(0);
    var notFoundCount by mutableStateOf(0);
    val questions = mutableStateListOf<QuizQuestion>();
    var current by mutableStateOf(0);
    var selected by mutableStateOf<Int?>(null);
    var correctCount by mutableStateOf(0);
    val missed = mutableStateListOf<String>();
    var practice by mutableStateOf(false);

    private fun today(): Long {
        return LocalDate.now().toEpochDay();
    }

    private suspend fun reload() {
        cards = buildCards(wordDao.all());
        states = HashMap(reviewDao.all().associateBy { it.wordKey });
        totalCount = cards.size;
        dueCount = dueCount(cards, states, today());
        phase = if(cards.isEmpty()) {QuizPhase.EMPTY} else {QuizPhase.IDLE};
    }

    fun refresh() {
        if(phase == QuizPhase.QUESTION || phase == QuizPhase.FINISHED) {
            return;
        }
        viewModelScope.launch {
            reload();
        };
    }

    fun start(practiceMode: Boolean) {
        viewModelScope.launch {
            phase = QuizPhase.LOADING;
            reload();
            practice = practiceMode;
            notFoundCount = 0;
            questions.clear();
            for(card in sessionOrder(cards, states, today(), practiceMode)) {
                if(questions.size >= 10) {
                    break;
                }
                val content = generator.load(card);
                if(content == null) {
                    ++notFoundCount;
                    continue;
                }
                val question = generator.generate(content, states[card.key] ?: ReviewState(card.key));
                if(question != null) {
                    questions.add(question);
                }
            }
            current = 0;
            selected = null;
            correctCount = 0;
            missed.clear();
            Log.d("QuizViewModel", "session: " + questions.size + " questions, " + notFoundCount + " not found");
            phase = if(questions.isEmpty()) {QuizPhase.IDLE} else {QuizPhase.QUESTION};
        };
    }

    fun answer(index: Int) {
        if(selected != null) {
            return;
        }
        selected = index;
        val question = questions[current];
        val correct = index == question.correctIndex;
        if(correct) {
            ++correctCount;
        } else {
            missed.add(question.lemma);
        }
        viewModelScope.launch {
            val updated = afterAnswer(states[question.cardKey] ?: ReviewState(question.cardKey), correct, today(), practice);
            states[question.cardKey] = updated;
            reviewDao.upsert(updated);
        };
    }

    fun next() {
        if(current + 1 < questions.size) {
            ++current;
            selected = null;
        } else {
            phase = QuizPhase.FINISHED;
        }
    }

    fun finish() {
        viewModelScope.launch {
            reload();
        };
    }
}
