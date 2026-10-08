package com.example.lxicon.ui.home

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lxicon.data.PalavrasViewModel
import com.example.lxicon.data.SaveResult
import com.example.lxicon.data.StoredWord
import com.example.lxicon.data.phraseContainsWord
import com.example.lxicon.data.dictionary.DictionaryViewModel

@Composable
fun Home() {
    val wordInput = rememberTextFieldState();
    val phraseInput = rememberTextFieldState();
    val palavrasViewModel: PalavrasViewModel = viewModel();
    val dictionaryViewModel: DictionaryViewModel = viewModel();
    var estado by rememberSaveable() { mutableStateOf(false) };
    var selectedSense by rememberSaveable() { mutableStateOf<String?>(null) };
    var warning by remember { mutableStateOf<String?>(null) };
    val activity = LocalContext.current as? Activity;
    DisposableEffect(Unit) {
        onDispose {
            if(activity?.isChangingConfigurations != true) {
                dictionaryViewModel.clear();
            }
        }
    }
    val hasResults = dictionaryViewModel.results.isNotEmpty();
    Column(Modifier.fillMaxSize(), verticalArrangement=if(hasResults) {Arrangement.Top} else {Arrangement.Center}, horizontalAlignment=Alignment.CenterHorizontally) {
        Row(verticalAlignment=Alignment.CenterVertically, modifier=Modifier.padding(6.dp)) {
            OutlinedTextField(state=if(estado==false) {wordInput} else {phraseInput}, placeholder ={if(estado==false) {Text("Insira aqui a palavra")} else {Text("Insira a frase (opcional):")} }, lineLimits=TextFieldLineLimits.SingleLine,modifier=Modifier.padding(6.dp));
            Button(shape=RectangleShape, onClick={
                if(estado==false && !wordInput.text.isBlank()) {
                    warning = null;
                    selectedSense = null;
                    dictionaryViewModel.search(wordInput.text.toString()) { found ->
                        if(found) {
                            estado = true;
                        }
                    };
                }
                else if(estado==true) {
                    val word = wordInput.text.toString();
                    val phrase = phraseInput.text.toString();
                    var message: String? = null;
                    var finished = false;
                    if(phrase.isBlank()) {
                        finished = true;
                        if(palavrasViewModel.save(StoredWord(word)) == SaveResult.DUPLICATE) {
                            message = "Palavra já salva";
                        }
                    } else if(selectedSense == null) {
                        message = "Toque no sentido ao qual a frase pertence";
                    } else if(!phraseContainsWord(phrase, word)) {
                        message = "A frase precisa conter a palavra \"" + word + "\"";
                    } else {
                        val result = palavrasViewModel.save(StoredWord(word, phrase, selectedSense));
                        if(result == SaveResult.SAVED) {
                            finished = true;
                        } else {
                            message = "Esse sentido já tem uma frase; edite-a na aba Palavras";
                        }
                    }
                    warning = message;
                    if(finished) {
                        estado = false;
                        selectedSense = null;
                        wordInput.clearText();
                        phraseInput.clearText();
                        dictionaryViewModel.clear();
                    }
                }
            }) {
                Text("->");
            };
        }
        if(warning != null) {
            Text(warning!!, color=MaterialTheme.colorScheme.error, modifier=Modifier.padding(12.dp));
        }
        if(dictionaryViewModel.notFound) {
            Text("Palavra não encontrada no dicionário", modifier=Modifier.padding(12.dp));
        }
        if(hasResults) {
            LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal=12.dp)) {
                items(dictionaryViewModel.results) { result ->
                    Column {
                        Text(result.entry.word + " (" + result.entry.partOfSpeech + ")", fontWeight=FontWeight.Bold, modifier=Modifier.padding(top=12.dp));
                        result.senses.forEach { sense ->
                            val label = if(sense.label != null) {"[" + sense.label + "] "} else {""};
                            val background = if(sense.id == selectedSense) {MaterialTheme.colorScheme.primaryContainer} else {Color.Transparent};
                            Text(sense.order.toString() + ". " + label + sense.definition, modifier=Modifier.fillMaxWidth().background(background).clickable(enabled=estado) { selectedSense = sense.id }.padding(vertical=4.dp));
                        }
                    }
                }
            }
        }
    }
}
