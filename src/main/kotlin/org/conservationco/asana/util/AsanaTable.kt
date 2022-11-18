package org.conservationco.asana.util

import com.asana.models.Project
import com.asana.models.Task
import org.conservationco.asana.asanaContext
import org.conservationco.asana.requests.Action
import org.conservationco.asana.serialization.AsanaSerializable
import kotlin.reflect.KClass

/**
 * This class represents a table over the tasks contained in an Asana project, supporting bidirectional and automatic
 * conversion between tasks and a specified type.
 *
 * @param T The type of object that encapsulates data contained in tasks belonging to this project.
 */
class AsanaTable<T : AsanaSerializable<T>> @PublishedApi internal constructor(
    val projectGid: String,
    private val includeAttachments: Boolean = false,
    private val destinationClass: KClass<out T>,
    private val serializingFn: (T, Task) -> Unit = { _, _ -> },
    private val deserializingFn: (Task, T) -> Unit = { _, _ -> },
) : Table<String, T> {

    companion object {
        /**
         * Using the given arguments, instantiates and returns an [AsanaTable] that serializes objects of the given type
         * [T].
         */
        inline fun <reified T : AsanaSerializable<T>> tableFor(
            projectGid: String,
            includeAttachments: Boolean = false,
            noinline serializingFn: (T, Task) -> Unit = { _, _ -> },
            noinline deserializingFn: (Task, T) -> Unit = { _, _ -> },
        ) = AsanaTable(projectGid, includeAttachments, T::class, serializingFn, deserializingFn)
    }

    /**
     * Stores the project that this [AsanaTable] provides an interface over.
     */
    private val project = Project().apply { gid = projectGid }

// CREATE functions

    /**
     * Creates the given [entry] on this project, returning the String global identifier of the created [Task].
     */
    override fun create(entry: T): String = asanaContext {
        project.run {
            val task = entry.convertToTask(this, serializingFn)
            createTask(task).gid
        }
    }

// GET functions

    /**
     * Returns the result of deserializing the task associated with the given global identifier [key].
     */
    override fun get(key: String): T = asanaContext {
        task(key)
            .get(includeAttachments)
            .convertTo(destinationClass, deserializingFn)
    }

    /**
     * Returns the result of deserializing all tasks associated with the Asana project for this table.
     */
    override fun getAll(): List<T> = asanaContext {
        project.convertTasksToListOf(destinationClass, includeAttachments, deserializingFn)
    }

    /**
     * Polls the event stream of the Asana project this table wraps, returning an `Iterable` result of deserializing all
     * task events that pass filtering by the given [actions].
     */
    fun getRecentlyAdded(vararg actions: Action): Iterable<T> = asanaContext {
        project.run {
            val tasks = pollEventStream(includeAttachments, *actions)
            tasks.convertToListOf(destinationClass, this, deserializingFn)
        }
    }

// UPDATE functions

    /**
     * Updates the given [entry] on the Asana project for this table.
     */
    override fun update(entry: T): Unit = asanaContext {
        val task = entry.convertToTask(project, serializingFn)
        task.update()
    }

// DELETE functions

    /**
     * Deletes the task on this project associated with the given [key].
     */
    override fun delete(key: String): Unit = asanaContext {
        val task = task(key)
        task.delete()
    }

// Utility functions

    /**
     * Returns the amount of tasks associated with the Asana project for this table.
     */
    override fun size(): Int = asanaContext { project.getTaskCount() }

}
