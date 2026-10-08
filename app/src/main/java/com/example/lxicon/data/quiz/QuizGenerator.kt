package com.example.lxicon.data.quiz

import com.example.lxicon.data.ReviewState
import com.example.lxicon.data.dictionary.DictionaryDao
import com.example.lxicon.data.dictionary.Entry
import com.example.lxicon.data.dictionary.resolveEntries
import kotlin.random.Random

class QuizGenerator(private val dao: DictionaryDao, private val random: Random) {
    suspend fun load(card: QuizCard): CardContent? {
        val entries = resolveEntries(dao, card.word);
        if(entries.isEmpty()) {
            return null;
        }
        val senses = entries.map { it.id }.chunked(500).flatMap { dao.sensesOf(it) };
        val senseIds = senses.map { it.id };
        val synonyms = senseIds.chunked(500).flatMap { dao.synonymsOf(it) };
        val examples = senseIds.chunked(500).flatMap { dao.examplesOf(it) };
        return CardContent(card, entries, senses, synonyms, examples);
    }

    suspend fun generate(content: CardContent, state: ReviewState): QuizQuestion? {
        typeOrder(content, state).forEach { type ->
            val question = when(type) {
                QuestionType.MEANING -> meaning(content, state);
                QuestionType.WORD -> word(content, state);
                QuestionType.CLOZE -> cloze(content, state);
                QuestionType.SYNONYMS -> synonyms(content, state);
            };
            if(question != null) {
                return question;
            }
        }
        return null;
    }

    private fun lemmaLabel(content: CardContent, lemma: String): String {
        if(textKey(lemma) == content.card.key) {
            return lemma;
        }
        return lemma + " (você salvou: " + content.card.word + ")";
    }

    private suspend fun toCandidates(entries: List<Entry>, target: Entry): List<DistractorCandidate> {
        val senses = entries.map { it.id }.chunked(500).flatMap { dao.sensesOf(it) };
        val synonyms = senses.map { it.id }.chunked(500).flatMap { dao.synonymsOf(it) };
        val byEntry = senses.groupBy { it.entryId };
        return entries.map { entry ->
            val own = byEntry[entry.id] ?: emptyList();
            val words = own.flatMap { synonymWordsOf(it, synonyms) }.distinct();
            DistractorCandidate(entry, own, words, maxOf(commonPrefix(entry.key, target.key), commonSuffix(entry.key, target.key)))
        }.filter { it.senses.isNotEmpty() };
    }

    private suspend fun neighbours(target: Entry, limit: Int): List<DistractorCandidate> {
        val partOfSpeech = target.partOfSpeech;
        val prefix = target.key.take(2);
        val upper = if(prefix.isEmpty()) {""} else {prefix.dropLast(1) + (prefix.last() + 1)};
        val entries = (dao.suffixNeighborsAfter(partOfSpeech, target.reversedKey, limit)
            + dao.suffixNeighborsBefore(partOfSpeech, target.reversedKey, limit)
            + dao.prefixNeighborsAfter(target.key, upper, partOfSpeech, limit)
            + dao.prefixNeighborsBefore(target.key, prefix, partOfSpeech, limit)).distinctBy { it.key };
        return toCandidates(entries, target);
    }

    private suspend fun randomCandidates(target: Entry): List<DistractorCandidate> {
        val pivot = ('a' + random.nextInt(26)).toString() + ('a' + random.nextInt(26)).toString();
        var entries = dao.suffixNeighborsAfter(target.partOfSpeech, pivot, 40);
        if(entries.size < 40) {
            entries = entries + dao.suffixNeighborsAfter(target.partOfSpeech, "", 40 - entries.size);
        }
        return toCandidates(entries.distinctBy { it.key }, target).map { it.copy(score = 0) };
    }

    private suspend fun similar(target: Entry, senseId: String?): List<DistractorCandidate> {
        if(senseId == null) {
            return emptyList();
        }
        val terms = dao.termsOf(senseId);
        if(terms.size < 2) {
            return emptyList();
        }
        val matches = dao.similarSenses(terms, target.partOfSpeech, target.id, 60).filter { it.shared < terms.size };
        if(matches.isEmpty()) {
            return emptyList();
        }
        val matchedSenses = matches.map { it.senseId }.chunked(500).flatMap { dao.sensesByIds(it) }.associateBy { it.id };
        val entries = matchedSenses.values.map { it.entryId }.distinct().chunked(500).flatMap { dao.entriesByIds(it) };
        val candidates = toCandidates(entries, target);
        return candidates.mapNotNull { candidate ->
            val best = matches.filter { matchedSenses[it.senseId]?.entryId == candidate.entry.id }.maxByOrNull { it.shared } ?: return@mapNotNull null;
            candidate.copy(score = 100 + best.shared * 10, matchedSenseId = best.senseId)
        };
    }

    private suspend fun pool(target: Entry, content: CardContent, extraVetoKeys: Set<String>, senseId: String?): List<DistractorCandidate> {
        val semantic = similar(target, senseId).filter { !entryVetoed(it, target, content, extraVetoKeys) };
        var eligible = (semantic + neighbours(target, 30).filter { !entryVetoed(it, target, content, extraVetoKeys) }).distinctBy { it.entry.key };
        if(eligible.size < 12) {
            eligible = (semantic + neighbours(target, 150).filter { !entryVetoed(it, target, content, extraVetoKeys) }).distinctBy { it.entry.key };
        }
        if(eligible.size < 6) {
            eligible = (eligible + randomCandidates(target).filter { !entryVetoed(it, target, content, extraVetoKeys) }).distinctBy { it.entry.key };
        }
        return eligible;
    }

    private suspend fun meaning(content: CardContent, state: ReviewState): QuizQuestion? {
        val sense = pickSense(content, state.timesAsked) ?: return null;
        val target = content.entries.firstOrNull { it.id == sense.entryId } ?: return null;
        val correct = sense.definition;
        val scored = pool(target, content, emptySet(), sense.id).mapNotNull { candidate ->
            val usable = candidate.senses.filter {
                it.type == "sentido" && referencedWords(it.definition).isEmpty() && it.noise == 0 && !isGentilic(it.definition) && it.definition.length >= 10 && findSpans(it.definition, target.word).isEmpty()
            };
            val best = usable.firstOrNull { it.id == candidate.matchedSenseId } ?: usable.minByOrNull { Math.abs(it.definition.length - correct.length) };
            if(best == null) {null} else {Pair(candidate.score, best.definition)}
        };
        val distractors = chooseOptions(scored, correct, random) ?: return null;
        val (options, correctIndex) = assembleOptions(correct, distractors, random);
        return QuizQuestion(content.card.key, target.word, QuestionType.MEANING, "O que significa " + lemmaLabel(content, target.word) + "?", null, options, correctIndex, target.word + ": " + correct);
    }

    private suspend fun word(content: CardContent, state: ReviewState): QuizQuestion? {
        val target = content.entries.first();
        val defSenses = definitionSenses(content.senses, content.synonyms);
        var promptText: String? = null;
        var promptEntry = target;
        var promptSenseId: String? = null;
        var promptSynonymKeys = emptySet<String>();
        if(defSenses.isNotEmpty()) {
            val first = pickSense(content, state.timesAsked);
            val ordered = (listOfNotNull(first) + defSenses).distinct();
            val usable = ordered.firstOrNull { !givesAway(it.definition, content.entries.first { entry -> entry.id == it.entryId }.word) };
            if(usable != null) {
                promptText = usable.definition;
                promptEntry = content.entries.first { it.id == usable.entryId };
                promptSenseId = usable.id;
            }
        } else {
            val words = cardSynonymWords(content);
            if(words.isNotEmpty()) {
                promptText = "sinônimos: " + words.take(3).joinToString(", ");
                promptSynonymKeys = targetSynonymKeys(content);
            }
        }
        if(promptText == null) {
            return null;
        }
        val scored = pool(promptEntry, content, promptSynonymKeys, promptSenseId).filter { candidate ->
            candidateSynonymKeys(candidate).intersect(promptSynonymKeys).isEmpty()
        }.map { Pair(it.score, it.entry.word) };
        val distractors = chooseOptions(scored, promptEntry.word, random) ?: return null;
        val (options, correctIndex) = assembleOptions(promptEntry.word, distractors, random);
        return QuizQuestion(content.card.key, promptEntry.word, QuestionType.WORD, "Qual palavra corresponde a isto?", promptText, options, correctIndex, promptEntry.word + ": " + promptText);
    }

    private suspend fun formsFor(text: String): Map<String, Set<String>> {
        val keys = tokenRanges(text).map { textKey(text.substring(it.first, it.last + 1)) }.distinct();
        val forms = keys.chunked(500).flatMap { dao.formsByKeys(it) };
        return forms.groupBy({ it.key }, { textKey(it.lemma) }).mapValues { it.value.toSet() };
    }

    private suspend fun cloze(content: CardContent, state: ReviewState): QuizQuestion? {
        val sources = ArrayList<ClozeSource>();
        val phrases = content.card.phrases;
        for(shift in phrases.indices) {
            val phrase = phrases[(state.timesAsked + shift) % phrases.size];
            val span = findSpans(phrase.phrase, content.card.word).firstOrNull() ?: continue;
            val owner = content.senses.firstOrNull { it.id == phrase.senseId }?.let { sense -> content.entries.firstOrNull { it.id == sense.entryId } } ?: content.entries.first();
            sources.add(ClozeSource(phrase.phrase, span, owner, phrase.senseId));
            break;
        }
        if(sources.isEmpty()) {
            val linkedIds = phrases.mapNotNull { it.senseId }.toSet();
            val ordered = content.examples.sortedBy { if(linkedIds.contains(it.senseId)) {0} else {1} }.take(10);
            for(example in ordered) {
                val owner = content.senses.firstOrNull { it.id == example.senseId }?.let { sense -> content.entries.firstOrNull { it.id == sense.entryId } } ?: continue;
                val span = exampleSpan(example, owner.word, formsFor(example.text)) ?: continue;
                sources.add(ClozeSource(example.text, span, owner, example.senseId));
                break;
            }
        }
        if(sources.isEmpty()) {
            return null;
        }
        val (text, span, target, senseId) = sources.first();
        val blankKey = textKey(text.substring(span.first, span.last + 1));
        val extra = (dao.lemmasByKey(blankKey).map { textKey(it) } + dao.entriesByKey(blankKey).map { it.key }).toSet() - target.key;
        val scored = pool(target, content, extra, senseId).map { Pair(it.score, it.entry.word) };
        val distractors = chooseOptions(scored, target.word, random) ?: return null;
        val (options, correctIndex) = assembleOptions(target.word, distractors, random);
        return QuizQuestion(content.card.key, target.word, QuestionType.CLOZE, "Complete a frase (opções na forma de dicionário):", blankOut(text, listOf(span)), options, correctIndex, text + " → " + target.word);
    }

    private suspend fun synonyms(content: CardContent, state: ReviewState): QuizQuestion? {
        val words = cardSynonymWords(content);
        if(words.isEmpty()) {
            return null;
        }
        val target = content.entries.first();
        val wanted = minOf(3, words.size);
        val rotated = (0 until words.size).map { words[(state.timesAsked + it) % words.size] };
        val blocked = targetSynonymKeys(content) + target.key;
        val eligible = pool(target, content, emptySet(), null).filter { candidateSynonymKeys(it).intersect(blocked).isEmpty() };
        for(size in wanted downTo 1) {
            val correct = rotated.take(size).joinToString(", ");
            val scored = eligible.filter { it.synonymWords.size >= size }.map { Pair(it.score, it.synonymWords.take(size).joinToString(", ")) };
            val distractors = chooseOptions(scored, correct, random) ?: continue;
            val (options, correctIndex) = assembleOptions(correct, distractors, random);
            return QuizQuestion(content.card.key, target.word, QuestionType.SYNONYMS, "Quais são sinônimos de " + lemmaLabel(content, target.word) + "?", null, options, correctIndex, target.word + ": " + correct);
        }
        return null;
    }
}
