package com.example.lxicon.data.quiz

import com.example.lxicon.data.ReviewState
import com.example.lxicon.data.dictionary.Entry
import com.example.lxicon.data.dictionary.Example
import com.example.lxicon.data.dictionary.Sense
import com.example.lxicon.data.dictionary.Synonym
import kotlin.random.Random

fun synonymWordsOf(sense: Sense, synonyms: List<Synonym>): List<String> {
    if(sense.type == "sinonimos") {
        return synonyms.filter { it.senseId == sense.id }.sortedBy { it.order }.map { it.word };
    }
    return referencedWords(sense.definition);
}

fun isSynonymSense(sense: Sense, synonyms: List<Synonym>): Boolean {
    return synonymWordsOf(sense, synonyms).isNotEmpty();
}

fun definitionSenses(senses: List<Sense>, synonyms: List<Synonym>): List<Sense> {
    return senses.filter { it.type == "sentido" && !isSynonymSense(it, synonyms) };
}

fun cardSynonymWords(content: CardContent): List<String> {
    val lemmaKeys = content.entries.map { it.key }.toSet();
    return content.senses.flatMap { synonymWordsOf(it, content.synonyms) }.distinctBy { textKey(it) }.filter { !lemmaKeys.contains(textKey(it)) };
}

fun withoutReflexive(key: String): List<String> {
    if(key.endsWith("-se")) {
        return listOf(key, key.removeSuffix("-se"));
    }
    return listOf(key);
}

fun targetSynonymKeys(content: CardContent): Set<String> {
    return cardSynonymWords(content).flatMap { withoutReflexive(textKey(it)) }.toSet();
}

fun targetDefinitionSet(content: CardContent): Set<String> {
    return content.senses.map { normDef(it.definition) }.toSet();
}

fun candidateSynonymKeys(candidate: DistractorCandidate): Set<String> {
    return candidate.synonymWords.flatMap { withoutReflexive(textKey(it)) }.toSet();
}

fun entryVetoed(candidate: DistractorCandidate, target: Entry, content: CardContent, extraVetoKeys: Set<String>): Boolean {
    val key = candidate.entry.key;
    if(content.entries.any { it.key == key } || extraVetoKeys.contains(key)) {
        return true;
    }
    if(targetSynonymKeys(content).contains(key)) {
        return true;
    }
    if(candidateSynonymKeys(candidate).contains(target.key)) {
        return true;
    }
    val definitions = targetDefinitionSet(content);
    if(candidate.senses.any { definitions.contains(normDef(it.definition)) }) {
        return true;
    }
    val shortest = minOf(key.length, target.key.length);
    val allowed = if(shortest >= 7) {2} else if(shortest >= 5) {1} else {0};
    return levenshtein(key, target.key) <= allowed;
}

fun chooseOptions(scored: List<Pair<Int, String>>, correct: String, random: Random): List<String>? {
    val correctNorm = normDef(correct);
    val seen = HashSet<String>();
    seen.add(correctNorm);
    val unique = scored.shuffled(random).sortedByDescending { it.first }.filter { seen.add(normDef(it.second)) };
    val chosen = unique.take(6).map { it.second }.shuffled(random).take(3);
    if(chosen.size < 3) {
        return null;
    }
    return chosen;
}

fun assembleOptions(correct: String, distractors: List<String>, random: Random): Pair<List<String>, Int> {
    val options = (listOf(correct) + distractors).shuffled(random);
    return Pair(options, options.indexOf(correct));
}

fun givesAway(definition: String, lemma: String): Boolean {
    if(findSpans(definition, lemma).isNotEmpty()) {
        return true;
    }
    val stem = textKey(lemma).let { it.take(maxOf(4, it.length - 3)) };
    return tokenRanges(definition).any { textKey(definition.substring(it.first, it.last + 1)).startsWith(stem) };
}

fun pickSense(content: CardContent, timesAsked: Int): Sense? {
    val defSenses = definitionSenses(content.senses, content.synonyms);
    if(defSenses.isEmpty()) {
        return null;
    }
    val linkedIds = content.card.phrases.mapNotNull { it.senseId }.distinct();
    val linked = defSenses.filter { linkedIds.contains(it.id) };
    if(linked.isNotEmpty() && timesAsked % 2 == 0) {
        return linked[(timesAsked / 2) % linked.size];
    }
    val rotation = defSenses.take(5);
    return rotation[timesAsked % rotation.size];
}

fun typeOrder(content: CardContent, state: ReviewState): List<QuestionType> {
    val hasDefinitions = definitionSenses(content.senses, content.synonyms).isNotEmpty();
    val hasSynonyms = cardSynonymWords(content).isNotEmpty();
    val recognition = if(!hasDefinitions) {
        listOf(QuestionType.SYNONYMS)
    } else if(hasSynonyms && state.timesAsked % 3 == 2) {
        listOf(QuestionType.SYNONYMS, QuestionType.MEANING)
    } else {
        listOf(QuestionType.MEANING)
    };
    val production = if(state.timesAsked % 2 == 0) {
        listOf(QuestionType.CLOZE, QuestionType.WORD)
    } else {
        listOf(QuestionType.WORD, QuestionType.CLOZE)
    };
    val ordered = if(state.box <= 1) {recognition + production} else {production + recognition};
    return (ordered + QuestionType.values().toList()).distinct();
}

fun exampleSpan(example: Example, lemma: String, formsByKey: Map<String, Set<String>>): IntRange? {
    val text = example.text;
    if(text.length > 200) {
        return null;
    }
    val lemmaKey = textKey(lemma);
    val start = example.highlightStart;
    val end = example.highlightEnd;
    if(start != null && end != null && start >= 0 && end <= text.length && start < end) {
        val key = textKey(text.substring(start, end));
        val startsOnWord = text[start].isLetterOrDigit() && (start == 0 || !text[start - 1].isLetterOrDigit());
        val endsOnWord = text[end - 1].isLetterOrDigit() && (end == text.length || !text[end].isLetterOrDigit());
        if(startsOnWord && endsOnWord && spanMatchesLemma(key, lemmaKey, formsByKey[key] ?: emptySet())) {
            return start until end;
        }
    }
    tokenRanges(text).forEach {
        val key = textKey(text.substring(it.first, it.last + 1));
        if(spanMatchesLemma(key, lemmaKey, formsByKey[key] ?: emptySet())) {
            return it;
        }
    }
    return findSpans(text, lemma).firstOrNull();
}
