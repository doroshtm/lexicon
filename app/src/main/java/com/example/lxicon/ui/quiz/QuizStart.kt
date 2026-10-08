package com.example.lxicon.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
fun QuizStart(dueCount: Int, totalCount: Int, notFoundCount: Int, onStart: () -> Unit, onPractice: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.Center, horizontalAlignment=Alignment.CenterHorizontally) {
        if(dueCount > 0) {
            Text("Para revisar hoje: " + dueCount.toString(), style=MaterialTheme.typography.titleMedium);
            Button(shape= RectangleShape,onClick=onStart, modifier=Modifier.padding(top=12.dp)) {
                Text("Começar");
            };
        } else {
            Text("Nada para revisar hoje", style=MaterialTheme.typography.titleMedium);
            Button(shape= RectangleShape, onClick=onPractice, modifier=Modifier.padding(top=12.dp)) {
                Text("Praticar mesmo assim");
            };
        }
        Text("Palavras salvas: " + totalCount.toString(), modifier=Modifier.padding(top=16.dp));
        if(notFoundCount > 0) {
            Text(notFoundCount.toString() + " palavra(s) não encontrada(s) no dicionário", color=MaterialTheme.colorScheme.error, modifier=Modifier.padding(top=8.dp));
        }
    }
}
