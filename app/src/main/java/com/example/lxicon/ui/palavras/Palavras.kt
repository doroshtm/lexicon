package com.example.lxicon.ui.palavras

import android.text.style.ForegroundColorSpan
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lxicon.data.PalavrasViewModel
import com.example.lxicon.data.StoredWord
import com.example.lxicon.data.phraseContainsWord

@Composable
fun Palavras() {
    val viewModel: PalavrasViewModel = viewModel()
    var wordEdit by remember() { mutableStateOf<StoredWord?>(null) };
    val wordSearch = rememberTextFieldState();
    val wordsList = viewModel.words.filter({it.word.contains(wordSearch.text.toString(), ignoreCase = true)});
    TextField(wordSearch, placeholder = {Text("Insira aqui a palavra a pesquisar")}, lineLimits=TextFieldLineLimits.SingleLine, modifier=Modifier.padding(6.dp))
    LazyColumn() {
        items(wordsList) { word ->
            val linkAnnotation = LinkAnnotation.Clickable(tag="teste", styles = TextLinkStyles(SpanStyle(color=Color.Blue, textDecoration=TextDecoration.Underline), pressedStyle = SpanStyle(color=Color.Magenta, textDecoration= TextDecoration.Underline)), linkInteractionListener= {
                wordEdit = word;
            });
            val texto = buildAnnotatedString {
                append(word.word + if(word.phrase != null) {" -> " + word.phrase} else {""});
                withLink(linkAnnotation) {append("   [editar palavra ou frase]")};
            }
            Column(Modifier.padding(vertical=4.dp)) {
                Text(texto);
                val definition = viewModel.senseDefinitions[word.senseId];
                if(definition != null) {
                    Text("Sentido: " + definition, style=MaterialTheme.typography.bodySmall);
                }
            }
        }
    };
    wordEdit?.let {word->
        val wordState = rememberTextFieldState(word.word);
        val phraseState = rememberTextFieldState(word.phrase ?: "");
        var phraseWarning by remember(word) { mutableStateOf(false) };

        AlertDialog(title={Text("Editar palavra " + word.word)}, text = {
            Column() {
                val definition = viewModel.senseDefinitions[word.senseId];
                if(definition != null) {
                    Text("Sentido: " + definition, style=MaterialTheme.typography.bodySmall);
                }
                TextField(wordState, lineLimits= TextFieldLineLimits.SingleLine, modifier=Modifier.padding(vertical = 19.dp));
                word.phrase.let {phrase ->
                    TextField(phraseState);
                }
                if(phraseWarning) {
                    Text("A frase precisa conter a palavra \"" + wordState.text + "\"", color=MaterialTheme.colorScheme.error, modifier=Modifier.padding(top=8.dp));
                }
            }
        }, onDismissRequest = {wordEdit=null}, confirmButton = {
            TextButton({
                        if(wordState.text.isBlank()) {
                            wordEdit=null;
                        } else if(!phraseState.text.isBlank() && !phraseContainsWord(phraseState.text.toString(), wordState.text.toString())) {
                            phraseWarning = true;
                        } else {
                            viewModel.update(word, StoredWord(wordState.text.toString(),if(!phraseState.text.isBlank()) {phraseState.text.toString()} else {null}));
                            wordEdit = null;
                        }
            }, content={Text("Editar")})
        }, dismissButton = {
                TextButton({
                    viewModel.delete(word);
                    wordEdit=null;
                }, content={Text("Deletar")})
            }
        );
    };
};
