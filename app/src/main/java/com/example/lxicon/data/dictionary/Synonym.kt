package com.example.lxicon.data.dictionary

import androidx.room3.ColumnInfo
import androidx.room3.Entity

@Entity(tableName="sinonimo", primaryKeys=["sentido_id", "ordem"])
data class Synonym(
    @ColumnInfo(name="sentido_id") val senseId: String,
    @ColumnInfo(name="ordem") val order: Int,
    @ColumnInfo(name="palavra") val word: String
);
