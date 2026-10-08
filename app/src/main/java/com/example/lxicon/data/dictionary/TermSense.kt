package com.example.lxicon.data.dictionary

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index

@Entity(tableName="termo_sentido", primaryKeys=["termo", "sentido_id"], withoutRowId=true, indices=[
    Index(value=["sentido_id", "termo"], name="index_termo_sentido_sentido_id")
])
data class TermSense(
    @ColumnInfo(name="termo") val term: String,
    @ColumnInfo(name="sentido_id") val senseId: String
);
