package com.example.lxicon.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName="review_state")
data class ReviewState(@PrimaryKey val wordKey: String, val box: Int=0, val dueDay: Long=0, val timesAsked: Int=0);
