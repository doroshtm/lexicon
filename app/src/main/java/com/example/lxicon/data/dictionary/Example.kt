package com.example.lxicon.data.dictionary

import androidx.room3.ColumnInfo
import androidx.room3.Entity

@Entity(tableName="exemplo", primaryKeys=["sentido_id", "ordem"])
data class Example(
    @ColumnInfo(name="sentido_id") val senseId: String,
    @ColumnInfo(name="ordem") val order: Int,
    @ColumnInfo(name="texto") val text: String,
    @ColumnInfo(name="destaque_inicio") val highlightStart: Int?=null,
    @ColumnInfo(name="destaque_fim") val highlightEnd: Int?=null
);
