package com.example.lxicon.data.dictionary

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName="verbete", indices=[
    Index(value=["chave"], name="index_verbete_chave"),
    Index(value=["classe", "chave_invertida"], name="index_verbete_classe_chave_invertida")
])
data class Entry(
    @PrimaryKey val id: Int,
    @ColumnInfo(name="palavra") val word: String,
    @ColumnInfo(name="classe") val partOfSpeech: String,
    @ColumnInfo(name="chave") val key: String,
    @ColumnInfo(name="chave_invertida") val reversedKey: String,
    @ColumnInfo(name="ruido") val noise: Int
);
