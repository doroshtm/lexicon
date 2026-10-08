package com.example.lxicon.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuizSummary(correct: Int, total: Int, missed: List<String>, onDone: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.Center, horizontalAlignment=Alignment.CenterHorizontally) {
        Text("Você acertou " + correct.toString() + " de " + total.toString(), style=MaterialTheme.typography.titleLarge);
        if(missed.isNotEmpty()) {
            Text("Para rever: " + missed.joinToString(", "), modifier=Modifier.padding(top=12.dp));
        }
        Button(onClick=onDone, modifier=Modifier.padding(top=16.dp)) {
            Text("Voltar");
        };
    }
}
