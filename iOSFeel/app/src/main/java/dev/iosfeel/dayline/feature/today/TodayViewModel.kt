package dev.iosfeel.dayline.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.iosfeel.dayline.core.model.TimelineItem
import dev.iosfeel.dayline.core.repository.NowItemState
import dev.iosfeel.dayline.core.repository.TaskRepository
import dev.iosfeel.dayline.core.repository.TimelineRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TodayUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val items: List<TimelineItem> = emptyList(),
    val nowState: NowItemState = NowItemState(
        title = "Plan your day",
        subtitle = "Set 3 priorities to get started",
        progressFraction = 0f
    ),
    val totalCount: Int = 0,
    val completedCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val taskRepository: TaskRepository,
    private val timelineRepository: TimelineRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val uiState: StateFlow<TodayUiState> = _selectedDate
        .flatMapLatest { date ->
            timelineRepository.getTimelineForDate(date).map { items ->
                val nowState = timelineRepository.calculateNowItem(items)
                val completed = items.count { it.isCompleted }
                TodayUiState(
                    selectedDate = date,
                    items = items,
                    nowState = nowState,
                    totalCount = items.size,
                    completedCount = completed
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TodayUiState()
        )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleTask(item: TimelineItem) {
        viewModelScope.launch {
            when (item) {
                is TimelineItem.TaskItem -> {
                    taskRepository.toggleTaskCompleted(item.task.id, item.task.completed)
                }
                else -> {
                    // Other item types will be connected in subsequent phases
                }
            }
        }
    }

    class Factory(
        private val taskRepository: TaskRepository,
        private val timelineRepository: TimelineRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TodayViewModel(taskRepository, timelineRepository) as T
        }
    }
}
