package com.example.lxicon.data

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface ReviewStateDao {
    @Query("SELECT * FROM review_state")
    suspend fun all(): List<ReviewState>;

    @Upsert
    suspend fun upsert(state: ReviewState);
}
