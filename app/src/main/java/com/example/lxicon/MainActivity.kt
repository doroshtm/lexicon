package com.example.lxicon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lxicon.ui.theme.LéxiconTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.lxicon.data.StoredWord
import com.example.lxicon.ui.home.Home
import com.example.lxicon.ui.palavras.Palavras
import com.example.lxicon.ui.quiz.Quiz


// - Screen-level composables (Home, Palavras, Quiz) get the ViewModel via viewModel().
// - Never pass the ViewModel down to child composables.
//   Children receive plain values (state) and lambdas (events) instead.
// - State flows down, events flow up. Only the screen calls ViewModel functions.
// - The ViewModel holds the data (list now, Room later) so it survives rotation.
// - MainActivity only handles the tabs; it doesn't wire data between screens.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LéxiconTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var currentTab by rememberSaveable {
                        mutableStateOf(1);
                    }
                    var StoredWords = remember { mutableStateListOf<StoredWord>() };
                    val tabs = listOf("Quiz","Home","Palavras");
                    //Greeting(name = "Android", modifier = Modifier.padding(innerPadding));
                    Column(modifier = Modifier.padding(innerPadding)) {
                        TabRow(selectedTabIndex = currentTab) {
                            tabs.forEachIndexed {index,tab->
                                Tab(selected=(currentTab==index), onClick={currentTab=index},text={Text(tab)});
                            }
                        }

                        when(currentTab) {
                            0->Quiz();
                            1->Home();
                            2->Palavras();
                        }
                    }
                }
            }
        }
    }
}
