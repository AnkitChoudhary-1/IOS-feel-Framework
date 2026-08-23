package dev.iosfeel.dayline.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.iosfeel.dayline.core.model.Expense
import dev.iosfeel.dayline.core.model.ExpenseCategory
import dev.iosfeel.dayline.core.model.Note
import dev.iosfeel.dayline.core.model.Task
import dev.iosfeel.dayline.core.model.TaskPriority
import dev.iosfeel.dayline.core.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

enum class CaptureType {
    Task,
    Event,
    Expense,
    Note
}

enum class DatePreset {
    Today,
    Tomorrow,
    Someday
}

enum class TimePreset(val time: LocalTime?) {
    None(null),
    Morning(LocalTime.of(9, 0)),
    Afternoon(LocalTime.of(14, 0)),
    Evening(LocalTime.of(19, 0))
}

data class CaptureDraft(
    val title: String = "",
    val type: CaptureType = CaptureType.Task,
    val datePreset: DatePreset = DatePreset.Today,
    val timePreset: TimePreset = TimePreset.None,
    val priority: TaskPriority = TaskPriority.None,
    val estimatedMinutes: Int? = null,
    val isValid: Boolean = false
)

class CaptureViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _draft = MutableStateFlow(CaptureDraft())
    val draft: StateFlow<CaptureDraft> = _draft.asStateFlow()

    fun onTitleChanged(newTitle: String) {
        _draft.update { it.copy(title = newTitle, isValid = newTitle.isNotBlank()) }
    }

    fun onTypeChanged(type: CaptureType) {
        _draft.update { it.copy(type = type) }
    }

    fun onDatePresetChanged(preset: DatePreset) {
        _draft.update { it.copy(datePreset = preset) }
    }

    fun onTimePresetChanged(preset: TimePreset) {
        _draft.update { it.copy(timePreset = preset) }
    }

    fun onPriorityChanged(priority: TaskPriority) {
        _draft.update { it.copy(priority = priority) }
    }

    fun save(onSuccess: () -> Unit) {
        val currentDraft = _draft.value
        if (!currentDraft.isValid) return

        viewModelScope.launch {
            val scheduledDate = when (currentDraft.datePreset) {
                DatePreset.Today -> LocalDate.now()
                DatePreset.Tomorrow -> LocalDate.now().plusDays(1)
                DatePreset.Someday -> null
            }

            when (currentDraft.type) {
                CaptureType.Task -> {
                    val task = Task(
                        title = currentDraft.title.trim(),
                        description = null,
                        scheduledDate = scheduledDate,
                        scheduledTime = currentDraft.timePreset.time,
                        dueDate = null,
                        estimatedMinutes = currentDraft.estimatedMinutes,
                        priority = currentDraft.priority,
                        completed = false,
                        completedAt = null,
                        reminderEnabled = currentDraft.timePreset.time != null,
                        createdAt = Instant.now(),
                        updatedAt = Instant.now()
                    )
                    taskRepository.createTask(task)
                }
                else -> {
                    // Default fallback to task for now
                    val task = Task(
                        title = currentDraft.title.trim(),
                        scheduledDate = scheduledDate,
                        scheduledTime = currentDraft.timePreset.time,
                        priority = currentDraft.priority,
                        completed = false
                    )
                    taskRepository.createTask(task)
                }
            }

            // Reset draft
            _draft.value = CaptureDraft()
            onSuccess()
        }
    }

    fun reset() {
        _draft.value = CaptureDraft()
    }

    class Factory(
        private val taskRepository: TaskRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CaptureViewModel(taskRepository) as T
        }
    }
}
