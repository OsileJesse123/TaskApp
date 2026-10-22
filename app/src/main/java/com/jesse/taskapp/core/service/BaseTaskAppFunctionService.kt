package com.jesse.taskapp.core.service

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.jesse.taskapp.core.di.Dispatcher
import com.jesse.taskapp.core.di.TaskAppDispatchers
import com.jesse.taskapp.core.domain.model.Task
import com.jesse.taskapp.core.domain.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
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
    @Inject
    @Dispatcher(TaskAppDispatchers.DEFAULT)
    internal lateinit var defaultDispatcher: CoroutineDispatcher

    /**
     * Search or get a list of tasks based on natural language query, date range of start to end date range.
     *
     *
     * @param query the search term or task title (e.g., "Work on article")
     * @param createdFromMillis the start of the time frame for the list of tasks within a specified period (this is optional)
     * @param createdToMillis the end of the time frame for the list of tasks within a specified period (this is optional)
     * @return A list of tasks who's title or description matches the query and within an optional time frame
     * */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getTasks(
        query: String,
        createdFromMillis: Long?,
        createdToMillis: Long?,
    ): List<Task> {
        val tasks = taskRepository.observeTasks(
            query = query,
            createdFromMillis = createdFromMillis,
            createdToMillis = createdToMillis,
        )
            .flowOn(defaultDispatcher)
            .catch {
                    e ->
                e.printStackTrace()
                // If it fails for some reason, just return an empty list
                emit(emptyList())
            }
            .first()

        if(tasks.isEmpty()){
            throw AppFunctionElementNotFoundException("There are no tasks available on the app!")
        }

        return tasks
    }

    /**
     * Gets a singular task based on the task id
     *
     * @param id a unique identifier for the task
     * @return a singular task that matches the id
     * */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getTask(
        id: Long
    ): Task{
        val task = taskRepository.observeTask(id)
            .catch {
                    e ->
                e.printStackTrace()
                emit(null)
            }.first()

        if(task == null){
            throw AppFunctionElementNotFoundException("Task not found for ID = $id")
        }

        return task
    }
}