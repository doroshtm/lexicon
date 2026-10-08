package com.example.lxicon.data.dictionary

import androidx.room3.Dao
import androidx.room3.Query

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM verbete WHERE chave = :key ORDER BY id")
    suspend fun entriesByKey(key: String): List<Entry>;

    @Query("SELECT * FROM sentido WHERE verbete_id IN (:entryIds) ORDER BY verbete_id, ordem")
    suspend fun sensesOf(entryIds: List<Int>): List<Sense>;

    @Query("SELECT lema FROM forma WHERE chave = :key ORDER BY lema")
    suspend fun lemmasByKey(key: String): List<String>;

    @Query("SELECT * FROM sentido WHERE id IN (:senseIds)")
    suspend fun sensesByIds(senseIds: List<String>): List<Sense>;

    @Query("SELECT * FROM verbete WHERE id IN (:entryIds)")
    suspend fun entriesByIds(entryIds: List<Int>): List<Entry>;

    @Query("SELECT * FROM verbete WHERE classe = :partOfSpeech AND chave_invertida > :reversedKey AND ruido = 0 ORDER BY chave_invertida LIMIT :limit")
    suspend fun suffixNeighborsAfter(partOfSpeech: String, reversedKey: String, limit: Int): List<Entry>;

    @Query("SELECT * FROM verbete WHERE classe = :partOfSpeech AND chave_invertida < :reversedKey AND ruido = 0 ORDER BY chave_invertida DESC LIMIT :limit")
    suspend fun suffixNeighborsBefore(partOfSpeech: String, reversedKey: String, limit: Int): List<Entry>;

    @Query("SELECT * FROM verbete WHERE chave > :key AND chave < :upper AND classe = :partOfSpeech AND ruido = 0 ORDER BY chave LIMIT :limit")
    suspend fun prefixNeighborsAfter(key: String, upper: String, partOfSpeech: String, limit: Int): List<Entry>;

    @Query("SELECT * FROM verbete WHERE chave < :key AND chave >= :lower AND classe = :partOfSpeech AND ruido = 0 ORDER BY chave DESC LIMIT :limit")
    suspend fun prefixNeighborsBefore(key: String, lower: String, partOfSpeech: String, limit: Int): List<Entry>;

    @Query("SELECT * FROM sinonimo WHERE sentido_id IN (:senseIds) ORDER BY sentido_id, ordem")
    suspend fun synonymsOf(senseIds: List<String>): List<Synonym>;

    @Query("SELECT * FROM exemplo WHERE sentido_id IN (:senseIds) ORDER BY sentido_id, ordem")
    suspend fun examplesOf(senseIds: List<String>): List<Example>;

    @Query("SELECT termo FROM termo_sentido WHERE sentido_id = :senseId")
    suspend fun termsOf(senseId: String): List<String>;

    @Query("SELECT t.sentido_id AS senseId, COUNT(*) AS shared FROM termo_sentido t JOIN sentido s ON s.id = t.sentido_id JOIN verbete v ON v.id = s.verbete_id WHERE t.termo IN (:terms) AND v.classe = :partOfSpeech AND v.ruido = 0 AND s.ruido = 0 AND s.tipo = 'sentido' AND s.verbete_id <> :entryId GROUP BY t.sentido_id ORDER BY shared DESC LIMIT :limit")
    suspend fun similarSenses(terms: List<String>, partOfSpeech: String, entryId: Int, limit: Int): List<SenseMatch>;

    @Query("SELECT * FROM forma WHERE chave IN (:keys)")
    suspend fun formsByKeys(keys: List<String>): List<WordForm>;
}
