package com.example.lxicon.data.dictionary

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import java.text.Normalizer

@Database(entities=[Entry::class, Sense::class, Synonym::class, Example::class, WordForm::class, Meta::class, TermSense::class], version=2, exportSchema=false)
abstract class DictionaryDatabase : RoomDatabase() {
    abstract fun dao(): DictionaryDao;

    companion object {
        @Volatile private var instance: DictionaryDatabase? = null;

        fun get(context: Context): DictionaryDatabase {
            return instance ?: synchronized(this) {
                context.applicationContext.deleteDatabase("dicionario.db");
                instance ?: Room.databaseBuilder(context.applicationContext, DictionaryDatabase::class.java, "dicionario-2.db").createFromAsset("database/dicionario.db").build().also { instance = it };
            };
        };

        fun searchKey(text: String): String {
            val decomposed = Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD);
            return decomposed.replace(Regex("\\p{M}+"), "");
        };
    }
}
