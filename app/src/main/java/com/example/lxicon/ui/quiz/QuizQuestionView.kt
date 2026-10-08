package com.example.lxicon.ui.quiz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lxicon.data.quiz.QuizQuestion

@Composable
fun QuizQuestionView(question: QuizQuestion, position: Int, total: Int, selected: Int?, onSelect: (Int) -> Unit, onNext: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(position.toString() + "/" + total.toString(), style=MaterialTheme.typography.labelMedium);
        Text(question.prompt, fontWeight=FontWeight.Bold, style=MaterialTheme.typography.titleMedium, modifier=Modifier.padding(top=8.dp));
        if(question.context != null) {
            Text(question.context, modifier=Modifier.padding(vertical=12.dp));
        }
        question.options.forEachIndexed { index, option ->
            val state = if(selected == null) {
                OptionState.NEUTRAL;
            } else if(index == question.correctIndex) {
                OptionState.CORRECT;
            } else if(index == selected) {
                OptionState.WRONG;
            } else {
                OptionState.NEUTRAL;
            };
            QuizOption(option, state, selected == null) { onSelect(index) };
        }
        if(selected != null) {
            Text(if(selected == question.correctIndex) {"Correto!"} else {"Errado"}, fontWeight=FontWeight.Bold, modifier=Modifier.padding(top=12.dp));
            Text(question.answerText, modifier=Modifier.padding(top=4.dp));
            Button(onClick=onNext, modifier=Modifier.padding(top=12.dp)) {
                Text("Próxima");
            };
        }
    }
}
