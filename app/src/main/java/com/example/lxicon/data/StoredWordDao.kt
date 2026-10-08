package com.example.lxicon.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update

@Dao
interface StoredWordDao {
    @Query("SELECT * FROM stored_word ORDER BY id")
    suspend fun all(): List<StoredWord>;

    @Insert
    suspend fun insert(word: StoredWord): Long;

    @Update
    suspend fun update(word: StoredWord);

    @Delete
    suspend fun delete(word: StoredWord);
}
