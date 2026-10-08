package com.example.lxicon.data.dictionary

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName="sentido", indices=[
    Index(value=["verbete_id"], name="index_sentido_verbete_id")
])
data class Sense(
    @PrimaryKey val id: String,
    @ColumnInfo(name="verbete_id") val entryId: Int,
    @ColumnInfo(name="ordem") val order: Int,
    @ColumnInfo(name="tipo") val type: String,
    @ColumnInfo(name="definicao") val definition: String,
    @ColumnInfo(name="rotulo") val label: String?=null,
    @ColumnInfo(name="ruido") val noise: Int
);
