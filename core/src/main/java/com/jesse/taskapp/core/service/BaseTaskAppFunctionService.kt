package com.jesse.taskapp.core.service

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.jesse.taskapp.core.common.CreationDateFilter
import com.jesse.taskapp.core.common.InvalidCreationDateException
import com.jesse.taskapp.core.common.TaskInputRules
import com.jesse.taskapp.core.common.TimeProvider
import com.jesse.taskapp.core.common.TitleError
import com.jesse.taskapp.core.di.Dispatcher
import com.jesse.taskapp.core.di.TaskAppDispatchers
import com.jesse.taskapp.core.domain.model.Task
import com.jesse.taskapp.core.domain.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@RequiresApi(36)
@AndroidEntryPoint
@AppFunctionServiceEntryPoint(
    serviceName = "TaskAppFunctionService",
    appFunctionXmlFileName = "task_app_function_service"
)
abstract class BaseTaskAppFunctionService: AppFunctionService() {

    @Inject internal lateinit var taskRepository: TaskRepository
    @Inject internal lateinit var creationDateFilter: CreationDateFilter
    @Inject internal lateinit var timeProvider: TimeProvider
    @Inject
    @Dispatcher(TaskAppDispatchers.DEFAULT)
    internal lateinit var defaultDispatcher: CoroutineDispatcher

    /**
     * Searches for tasks that match the query (title or description) within an optional creation-date
     * range. Returns every task when [query] is blank and no dates are set. When only one date is
     * set, the filter is open-ended on the other side.
     *
     * Call when the user wants to search for, list, or filter tasks by keywords and/or creation date.
     *
     * @param query search term matched against title and description; omit or leave blank for no text filter.
     * @param createdFrom optional inclusive start date in yyyy-MM-dd. Calendar days use the device default timezone.
     * @param createdTo optional inclusive end date in yyyy-MM-dd. The end day is included; bounds are converted to an exclusive upper timestamp at the start of the following local day.
     * @return tasks matching the query and creation window, newest first. An empty list means no tasks matched; that is not an error.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getTasks(
        query: String = "",
        createdFrom: String?,
        createdTo: String?,
    ): List<Task> {
        val bounds = try {
            creationDateFilter.resolve(createdFrom, createdTo)
        } catch (e: InvalidCreationDateException) {
            throw AppFunctionInvalidArgumentException(e.message ?: "Invalid creation date filter.")
        }
        val normalizedQuery = query.trim()

        val tasks = taskRepository.observeTasks(
            query = normalizedQuery,
            createdFromMillis = bounds.fromMillisInclusive,
            createdToMillis = bounds.toMillisExclusive,
        )
            .flowOn(defaultDispatcher)
            .first()

        return tasks
    }

    /**
     * Retrieves a specific task by its unique identifier.
     *
     * Call when the user wants to view or retrieve a particular task.
     *
     * @param id a unique identifier for the task
     * @return the task matching the supplied identifier.
     * @throws AppFunctionElementNotFoundException if no task exists with the given ID.
     * */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getTask(
        id: Long
    ): Task {
        return taskRepository.observeTask(id)
            .first() ?: throw AppFunctionElementNotFoundException("Task not found for ID = $id")
    }

    /**
     * Creates a new task and stores it on the device. When [idempotencyKey] was already used for a
     * successful create, returns that existing task instead of inserting a second row. Retries of
     * the same logical operation must send the same key even if the first response was lost; a
     * different title or description on retry still returns the first saved task, including when
     * the retry payload would fail validation on a first-time create.
     *
     * Call when the user wants to save a new task.
     *
     * @param title required short name on the first create for this key; leading and trailing spaces are trimmed before save.
     * @param description optional longer text; pass null or blank to store an empty description. Trimmed before save.
     * @param idempotencyKey caller-generated key that must stay the same across retries of this create.
     * The service does not generate keys. Blank keys after trim are rejected.
     * @return the newly created task, or the task from the first successful create for this key.
     * @throws AppFunctionInvalidArgumentException when the idempotency key is blank, or when title or description fail validation on the first create for that key.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun createTask(
        title: String,
        idempotencyKey: String,
        description: String? = null,
    ): Task {
        val trimmedKey = idempotencyKey.trim()
        if (trimmedKey.isEmpty()) {
            throw AppFunctionInvalidArgumentException("Idempotency key must not be blank.")
        }
        taskRepository.getByIdempotencyKey(trimmedKey)?.let { return it }
        val errors = TaskInputRules.validate(title, description.orEmpty())
        if (errors != null) {
            val message = when {
                errors.titleError == TitleError.Required -> "Title is required."
                errors.titleError == TitleError.TooLong ->
                    "Title must be ${TaskInputRules.TITLE_MAX_LENGTH} characters or fewer."
                errors.descriptionError != null ->
                    "Description must be ${TaskInputRules.DESCRIPTION_MAX_LENGTH} characters or fewer."
                else -> "Invalid task input."
            }
            throw AppFunctionInvalidArgumentException(message)
        }
        val trimmedTitle = title.trim()
        val trimmedDescription = description.orEmpty().trim()
        val nowMillis = timeProvider.nowMillis()
        val id = taskRepository.create(
            title = trimmedTitle,
            description = trimmedDescription,
            nowMillis = nowMillis,
            idempotencyKey = trimmedKey,
        )
        return taskRepository.observeTask(id)
            .flowOn(defaultDispatcher)
            .first()
            ?: throw AppFunctionElementNotFoundException("Task not found after create for ID = $id")
    }
}