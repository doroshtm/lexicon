package com.example.lxicon.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName="stored_word")
data class StoredWord(val word: String, val phrase: String?=null, val senseId: String?=null, @PrimaryKey(autoGenerate=true) val id: Long=0);
