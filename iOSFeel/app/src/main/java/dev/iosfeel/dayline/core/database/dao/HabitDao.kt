package dev.iosfeel.dayline.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.iosfeel.dayline.core.database.entity.HabitCompletionEntity
import dev.iosfeel.dayline.core.database.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit_completions WHERE completedDate = :date")
    fun getCompletionsForDate(date: LocalDate): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId")
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletionEntity): Long

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND completedDate = :date")
    suspend fun deleteCompletion(habitId: Long, date: LocalDate)
}
