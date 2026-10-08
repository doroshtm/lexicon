package com.example.lxicon.data.quiz

import java.text.Normalizer

val blankMarker = "_____";

private val marks = Regex("\\p{M}+");
private val tokenRegex = Regex("[\\p{L}\\p{M}\\p{N}]+");
private val referencePrefix = Regex("^(o mesmo que|mesmo que|vide|ver|veja|variante de|forma alternativa de|sinônimo de|grafia \\S+ de)\\s+", RegexOption.IGNORE_CASE);
private val gentilic = Regex("^(natural|habitante|nativo|pessoa nascida|indivíduo natural)\\b|\\b(cidade|município|estado|distrito|freguesia|concelho|província|vila) d[eoa]s?\\b|^(relativo|referente|pertencente) (a|à|ao) (cidade|município|estado)", RegexOption.IGNORE_CASE);

fun keyMap(text: String): KeyedText {
    val key = StringBuilder();
    val origin = ArrayList<Int>();
    var index = 0;
    while(index < text.length) {
        val codePoint = text.codePointAt(index);
        val piece = String(Character.toChars(codePoint)).lowercase();
        val stripped = marks.replace(Normalizer.normalize(piece, Normalizer.Form.NFD), "");
        stripped.forEach {
            key.append(it);
            origin.add(index);
        }
        index += Character.charCount(codePoint);
    }
    return KeyedText(key.toString(), origin.toIntArray());
}

fun textKey(text: String): String {
    return keyMap(text.trim()).key;
}

fun findSpans(text: String, word: String): List<IntRange> {
    val wordKey = textKey(word);
    if(wordKey.isEmpty()) {
        return emptyList();
    }
    val keyed = keyMap(text);
    val wholeWord = Regex("(?<![\\p{L}\\p{N}])" + Regex.escape(wordKey) + "(?![\\p{L}\\p{N}])");
    return wholeWord.findAll(keyed.key).map {
        val start = keyed.origin[it.range.first];
        val end = if(it.range.last + 1 < keyed.origin.size) {keyed.origin[it.range.last + 1]} else {text.length};
        start until end
    }.toList();
}

fun blankOut(text: String, spans: List<IntRange>): String {
    var result = text;
    spans.sortedByDescending { it.first }.forEach {
        result = result.substring(0, it.first) + blankMarker + result.substring(it.last + 1);
    }
    return result;
}

fun commonPrefix(a: String, b: String): Int {
    var size = 0;
    while(size < a.length && size < b.length && a[size] == b[size]) {
        ++size;
    }
    return size;
}

fun commonSuffix(a: String, b: String): Int {
    var size = 0;
    while(size < a.length && size < b.length && a[a.length - 1 - size] == b[b.length - 1 - size]) {
        ++size;
    }
    return size;
}

fun levenshtein(a: String, b: String): Int {
    var previous = IntArray(b.length + 1) { it };
    for(i in 1..a.length) {
        val current = IntArray(b.length + 1);
        current[0] = i;
        for(j in 1..b.length) {
            val cost = if(a[i - 1] == b[j - 1]) {0} else {1};
            current[j] = minOf(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + cost);
        }
        previous = current;
    }
    return previous[b.length];
}

fun normDef(definition: String): String {
    return textKey(definition).replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim();
}

fun isReferenceDefinition(definition: String): Boolean {
    return referencePrefix.containsMatchIn(definition.trim());
}

fun referencedWords(definition: String): List<String> {
    val trimmed = definition.trim();
    if(!referencePrefix.containsMatchIn(trimmed)) {
        return emptyList();
    }
    var rest = referencePrefix.replace(trimmed, "");
    val cut = rest.indexOfFirst { it == ':' || it == '(' };
    if(cut >= 0) {
        rest = rest.substring(0, cut);
    }
    return rest.split(",", ";", " ou ").map { it.trim().trimEnd('.').trim() }.filter { it.isNotEmpty() && it.split(" ").size <= 3 };
}

fun isGentilic(definition: String): Boolean {
    return gentilic.containsMatchIn(definition.trim());
}

fun tokenRanges(text: String): List<IntRange> {
    return tokenRegex.findAll(text).map { it.range }.toList();
}

fun spanMatchesLemma(spanKey: String, lemmaKey: String, formLemmaKeys: Set<String>): Boolean {
    if(spanKey == lemmaKey || formLemmaKeys.contains(lemmaKey)) {
        return true;
    }
    val sharedStart = commonPrefix(spanKey, lemmaKey);
    return sharedStart >= maxOf(3, lemmaKey.length - 2) && Math.abs(spanKey.length - lemmaKey.length) <= 4;
}
