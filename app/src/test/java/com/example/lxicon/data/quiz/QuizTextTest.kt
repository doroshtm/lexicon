package com.example.lxicon.data.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizTextTest {
    @Test
    fun textKeyStripsAccentsAndCase() {
        assertEquals("caquetico", textKey("Caquético"));
        assertEquals("acucar", textKey(" Açúcar "));
    }

    @Test
    fun findSpansMapsBackToOriginalText() {
        val text = "Ela é PROLIXA demais";
        val spans = findSpans(text, "prolixa");
        assertEquals(1, spans.size);
        assertEquals("PROLIXA", text.substring(spans[0].first, spans[0].last + 1));
    }

    @Test
    fun findSpansHandlesAccentsAndHyphens() {
        val text = "Que Café bom, queixou-se ontem";
        assertEquals("Café", text.substring(findSpans(text, "cafe")[0].first, findSpans(text, "cafe")[0].last + 1));
        assertEquals(1, findSpans(text, "queixou").size);
    }

    @Test
    fun findSpansRequiresWholeWord() {
        assertTrue(findSpans("Comprei um casaco", "casa").isEmpty());
        assertTrue(findSpans("As casas são lindas", "casa").isEmpty());
        assertEquals(1, findSpans("A casa é grande", "casa").size);
    }

    @Test
    fun findSpansHandlesMultiWordLemma() {
        val text = "Saiu de repente da sala";
        val span = findSpans(text, "de repente")[0];
        assertEquals("de repente", text.substring(span.first, span.last + 1));
    }

    @Test
    fun blankOutUsesFixedLengthMarker() {
        val text = "A casa é grande";
        assertEquals("A _____ é grande", blankOut(text, findSpans(text, "casa")));
    }

    @Test
    fun referencedWordsParsesReferenceDefinitions() {
        assertEquals(listOf("perto", "próximo"), referencedWords("o mesmo que perto, próximo"));
        assertEquals(listOf("peixe-agulha"), referencedWords("o mesmo que peixe-agulha: nome vulgar de peixes"));
        assertEquals(listOf("hialino"), referencedWords("vide hialino"));
        assertTrue(referencedWords("relativo à ou próprio da caquexia").isEmpty());
        assertTrue(isReferenceDefinition("ver foto"));
        assertFalse(isReferenceDefinition("aquele que vê"));
    }

    @Test
    fun gentilicDetection() {
        assertTrue(isGentilic("de Capixaba, município do Acre"));
        assertTrue(isGentilic("natural do Brasil ou seu habitante"));
        assertFalse(isGentilic("relativo à capital; da capital"));
    }

    @Test
    fun levenshteinCountsEdits() {
        assertEquals(1, levenshtein("projectil", "projetil"));
        assertEquals(0, levenshtein("casa", "casa"));
        assertEquals(3, levenshtein("kitten", "sitting"));
    }

    @Test
    fun commonAffixes() {
        assertEquals(7, commonPrefix("capitalista", "capitalino").coerceAtMost(7));
        assertEquals(3, commonSuffix("capitalino", "algelino").coerceAtMost(3));
    }

    @Test
    fun spanMatchesLemmaByFormOrStem() {
        assertTrue(spanMatchesLemma("casas", "casa", emptySet()));
        assertTrue(spanMatchesLemma("fui", "ir", setOf("ir", "ser")));
        assertFalse(spanMatchesLemma("mesa", "casa", emptySet()));
    }
}
