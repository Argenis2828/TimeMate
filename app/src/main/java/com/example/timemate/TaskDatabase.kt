package com.example.timemate

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// definición de la base de datos usando room
// se indica la entidad que se va a almacenar y la versión de la base de datos
@Database(entities = [TaskEntity::class], version = 2)
abstract class TaskDatabase : RoomDatabase() {

    // metodo que permite acceder al dao para realizar operaciones con las tareas
    abstract fun taskDao(): TaskDao

    companion object {

        // variable que mantiene una sola instancia de la base de datos
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        // metodo que devuelve la instancia de la base de datos
        // si no existe, la crea de forma segura
        fun getInstance(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {

                // construcción de la base de datos usando room
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "timemate_db"
                )
                    // permite recrear la base de datos si cambia la versión
                    // se usa para evitar errores durante el desarrollo
                    .fallbackToDestructiveMigration()

                    // permite ejecutar consultas en el hilo principal
                    // se usa solo para simplificar el proyecto académico
                    .allowMainThreadQueries()

                    // crea la base de datos
                    .build()

                // guarda la instancia creada para reutilizarla
                INSTANCE = instance
                instance
            }
        }
    }
}


