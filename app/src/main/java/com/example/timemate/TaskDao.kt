package com.example.timemate

import androidx.room.*

@Dao
interface TaskDao {

    @Insert
    fun insert(task: TaskEntity): Long

    @Update
    fun update(task: TaskEntity)

    @Delete
    fun delete(task: TaskEntity)

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getById(id: Int): TaskEntity?

    @Query("DELETE FROM tasks")
    fun deleteAll()
}
