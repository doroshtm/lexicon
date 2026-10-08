package com.example.lxicon.ui.quiz

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuizOption(text: String, state: OptionState, enabled: Boolean, onClick: () -> Unit) {
    val color = when(state) {
        OptionState.CORRECT -> MaterialTheme.colorScheme.primaryContainer;
        OptionState.WRONG -> MaterialTheme.colorScheme.errorContainer;
        OptionState.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant;
    };
    Card(onClick=onClick, enabled=enabled, colors=CardDefaults.cardColors(containerColor=color, disabledContainerColor=color), modifier=Modifier.fillMaxWidth().padding(vertical=4.dp)) {
        Text(text, modifier=Modifier.padding(12.dp));
    }
}
