package com.example.lxicon.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.execSQL

@Database(entities=[StoredWord::class, ReviewState::class], version=2, exportSchema=false)
abstract class UserDatabase : RoomDatabase() {
    abstract fun storedWordDao(): StoredWordDao;
    abstract fun reviewStateDao(): ReviewStateDao;

    companion object {
        @Volatile private var instance: UserDatabase? = null;

        private val migration1To2 = Migration(1, 2) { connection ->
            connection.execSQL("ALTER TABLE `stored_word` ADD COLUMN `senseId` TEXT");
            connection.execSQL("CREATE TABLE IF NOT EXISTS `review_state` (`wordKey` TEXT NOT NULL, `box` INTEGER NOT NULL, `dueDay` INTEGER NOT NULL, `timesAsked` INTEGER NOT NULL, PRIMARY KEY(`wordKey`))");
        };

        fun get(context: Context): UserDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, UserDatabase::class.java, "user.db").addMigrations(migration1To2).build().also { instance = it };
            };
        };
    }
}
