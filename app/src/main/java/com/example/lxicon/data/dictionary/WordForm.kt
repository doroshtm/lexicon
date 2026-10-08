package com.example.lxicon.data.dictionary

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index

@Entity(tableName="forma", primaryKeys=["forma", "lema"], indices=[
    Index(value=["chave"], name="index_forma_chave")
])
data class WordForm(
    @ColumnInfo(name="forma") val form: String,
    @ColumnInfo(name="chave") val key: String,
    @ColumnInfo(name="lema") val lemma: String,
    @ColumnInfo(name="origem") val origin: String
);
