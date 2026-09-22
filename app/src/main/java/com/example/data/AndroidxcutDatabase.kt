package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = false)
abstract class AndroidxcutDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: AndroidxcutDatabase? = null

        fun getDatabase(context: Context): AndroidxcutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AndroidxcutDatabase::class.java,
                    "androidxcut_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
