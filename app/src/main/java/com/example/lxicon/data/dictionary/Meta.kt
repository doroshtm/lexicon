package com.example.lxicon.data.dictionary

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName="meta")
data class Meta(
    @PrimaryKey @ColumnInfo(name="chave") val key: String,
    @ColumnInfo(name="valor") val value: String
);
