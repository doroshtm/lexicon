package com.example.lxicon.data.quiz

data class QuizQuestion(val cardKey: String, val lemma: String, val type: QuestionType, val prompt: String, val context: String?, val options: List<String>, val correctIndex: Int, val answerText: String);
