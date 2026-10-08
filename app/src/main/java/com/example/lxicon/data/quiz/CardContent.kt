package com.example.lxicon.data.quiz

import com.example.lxicon.data.dictionary.Entry
import com.example.lxicon.data.dictionary.Example
import com.example.lxicon.data.dictionary.Sense
import com.example.lxicon.data.dictionary.Synonym

data class CardContent(val card: QuizCard, val entries: List<Entry>, val senses: List<Sense>, val synonyms: List<Synonym>, val examples: List<Example>);
