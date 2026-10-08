package com.example.lxicon.data.quiz

import com.example.lxicon.data.dictionary.Entry

data class ClozeSource(val text: String, val span: IntRange, val entry: Entry, val senseId: String?);
