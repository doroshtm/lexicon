package com.example.lxicon.data.quiz

import com.example.lxicon.data.dictionary.Entry
import com.example.lxicon.data.dictionary.Sense

data class DistractorCandidate(val entry: Entry, val senses: List<Sense>, val synonymWords: List<String>, val score: Int, val matchedSenseId: String?=null);
