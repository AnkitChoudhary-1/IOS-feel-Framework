package dev.iosfeel.dayline.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.iosfeel.dayline.core.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY scheduledDate ASC, scheduledTime ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE scheduledDate = :date ORDER BY scheduledTime ASC, createdAt DESC")
    fun getTasksForDate(date: LocalDate): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE scheduledDate IS NULL AND completed = 0 ORDER BY createdAt DESC")
    fun getUnscheduledTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("UPDATE tasks SET completed = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, completed: Boolean, completedAt: Long?)
}
