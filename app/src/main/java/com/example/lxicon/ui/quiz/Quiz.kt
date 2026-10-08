package com.example.lxicon.ui.quiz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lxicon.data.quiz.QuizPhase
import com.example.lxicon.data.quiz.QuizViewModel

@Composable
fun Quiz() {
    val viewModel: QuizViewModel = viewModel();
    LaunchedEffect(Unit) {
        viewModel.refresh();
    }
    when(viewModel.phase) {
        QuizPhase.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center) {
            CircularProgressIndicator();
        };
        QuizPhase.EMPTY -> Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center) {
            Text("Salve palavras na aba Home para começar o quiz.");
        };
        QuizPhase.IDLE -> QuizStart(viewModel.dueCount, viewModel.totalCount, viewModel.notFoundCount, { viewModel.start(false) }, { viewModel.start(true) });
        QuizPhase.QUESTION -> QuizQuestionView(viewModel.questions[viewModel.current], viewModel.current + 1, viewModel.questions.size, viewModel.selected, { viewModel.answer(it) }, { viewModel.next() });
        QuizPhase.FINISHED -> QuizSummary(viewModel.correctCount, viewModel.questions.size, viewModel.missed, { viewModel.finish() });
    }
}
