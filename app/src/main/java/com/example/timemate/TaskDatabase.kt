package com.example.timemate

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class], version = 2)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "timemate_db"
                )
                    .fallbackToDestructiveMigration() // <- IMPORTANTE al subir la versión
                    .allowMainThreadQueries() // modo sencillo por ahora
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}

