package org.conservationco.asana.extensions

import com.asana.models.*
import org.conservationco.asana.AsanaConfig
import org.conservationco.asana.exception.CustomFieldException
import org.conservationco.asana.extensions.events.Action
import org.conservationco.asana.extensions.events.Event
import org.conservationco.asana.serialization.AsanaSerializable
import org.conservationco.asana.serialization.AsanaTaskSerializer
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext
import org.conservationco.asana.serialization.customfield.context.ProjectCustomFieldContext
import org.conservationco.asana.serialization.customfield.context.TaskCustomFieldContext
import org.conservationco.asana.serialization.customfield.context.WorkspaceCustomFieldContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Extension class for the `java-asana` [com.asana.Client], providing context-switching functions for working with
 * resources and (de)serializing [Task] objects to custom data objects.
 *
 * @property config Configuration for this object, including what client to select, verbosity of logs, and optional
 *                  fields to return.
 */
class AsanaClientExtension(private val config: AsanaConfig) {

    private val requestExecutor = RequestExecutor(config)
    private val contexts: MutableMap<String, CustomFieldContext> = ConcurrentHashMap()

// Webhook extension functions

    /**
     * Creates a webhook on the given Asana [resourceGid]. This webhook which will send the initial handshake request
     * and future POST requests to the specified [targetUrl].
     */
    fun createWebhook(resourceGid: String, targetUrl: String) =
        requestExecutor.webhooks.createWebhook(resourceGid, targetUrl)

    /**
     * Deletes the webhook at the given [webhookGid].
     */
    fun deleteWehbook(webhookGid: String) =
        requestExecutor.webhooks.deleteWebhook(webhookGid)

    /**
     * Deletes all the webhooks on the given Asana [resourceGid] within the specified [workspaceGid].
     */
    fun deleteWebhooks(resourceGid: String, workspaceGid: String) =
        requestExecutor.webhooks.deleteWebhooks(resourceGid, workspaceGid)

    /**
     * Returns the complete record of the given Asana [webhookGid].
     */
    fun getWebhook(webhookGid: String) =
        requestExecutor.webhooks.getWebhook(webhookGid)

    /**
     * Returns all webhooks on the given Asana [resourceGid] within the specified [workspaceGid].
     */
    fun getWebhooks(resourceGid: String, workspaceGid: String) =
        requestExecutor.webhooks.getWebhooks(resourceGid, workspaceGid)

// Task extension functions

    /**
     * Returns an Asana [Task] with given [taskGid] (globally unique identifier).
     */
    fun task(taskGid: String): Task = Task().initResource(taskGid)

    /**
     * Returns the result of calling the given [block] on an Asana [Task] with the given [taskGid].
     */
    inline fun <R> task(taskGid: String, block: Task.() -> R): R = task(taskGid).block()

    /**
     * Returns the expanded version of this task, including attachments depending on the [includeAttachments] flag.
     */
    fun Task.get(includeAttachments: Boolean = false): Task {
        val task = requestExecutor.tasks.getTask(this)
        if (includeAttachments) task.attachments = task.getAttachments()
        return task
    }

    /**
     * Deletes this task, returning an empty [Task] response.
     */
    fun Task.delete(): Task {
        return requestExecutor.tasks.deleteTask(this)
    }

    /**
     * Updates this task, returning the complete [Task] response object.
     */
    fun Task.update(): Task {
        return requestExecutor.tasks.updateTask(this, getContextFor(this))
    }

    /**
     * Returns all attachments associated with this [Task].
     *
     * This function _always_ returns complete [Attachment] response objects, regardless of the fields associated with
     * this [AsanaClientExtension]'s `expandedResponses` or `fields` configuration.
     */
    fun Task.getAttachments(): Collection<Attachment> {
        return if (attachments == null) requestExecutor.tasks.getAttachment(this)
        else attachments
    }

    /**
     * Creates and returns the given [attachment] on this [Task].
     */
    fun Task.createAttachment(attachment: Attachment): Attachment {
        return requestExecutor.tasks.createAttachment(this, attachment)
    }

    /**
     * Creates the given [attachments] on this [Task].
     */
    fun Task.createAttachments(attachments: Collection<Attachment>) {
        attachments.forEach { createAttachment(it) }
    }

    /**
     * Returns a [Collection] of [Project] objects that represent where this task is a member.
     */
    fun Task.getProjects(): Collection<Project> {
        return requestExecutor.tasks.getProjects(this)
    }

// Project extension functions

    /**
     * Returns an Asana [Project] with the given [projectGid] (globally unique identifier).
     */
    fun project(projectGid: String): Project = Project().initResource(projectGid)

    /**
     * Returns the result of calling the given [block] on an Asana [Project] with the given [projectGid].
     */
    inline fun <R> project(projectGid: String, block: Project.() -> R): R = project(projectGid).block()

    /**
     * Returns all [Task] objects associated with this [Project], including or excluding Task attachments depending on
     * the given [includeAttachments] flag.
     *
     * This function returns the complete list of tasks on this project, automatically collecting any pagination that
     * occurs.
     *
     * Tasks returned by this function have the fields supplied by this [AsanaClientExtension]'s `config.fields`
     * setting.
     *
     * @see AsanaConfig.fields
     * @see AsanaConfig.expandedResponses Overrides `fields` supplied to return all data associated with each task
     *                                    (not recommended – only for debugging purposes)
     */
    fun Project.getTasks(includeAttachments: Boolean = false): List<Task> {
        val tasks = requestExecutor.projects.getTasksPaginated(this)
        if (includeAttachments) {
            tasks
                .associateBy({ it }, { it.getAttachments() })
                .forEach { it.key.attachments = it.value }
        }
        return tasks
    }

    /**
     * Creates the given [task] on this [Project], returning a complete response object with the created task's `gid`.
     */
    fun Project.createTask(task: Task): Task {
        task.projects = listOf(this)
        val created = requestExecutor.tasks.createTask(task, getContextFor(this))
        if (task.attachments != null) task.attachments.forEach { created.createAttachment(it) }
        return created
    }

    /**
     * Returns a collection of all [CustomField] objects associated with this [Project].
     *
     * This includes:
     *  - Asana-created custom fields (such as `created_on`, `modified_by`, `assignee`, etc.)
     *  - Project-specific custom fields
     *  - Workspace-global custom fields
     *
     * `CustomField` objects returned by this function have the fields supplied by this [AsanaClientExtension]'s `
     * config.fields` setting.
     *
     * @see AsanaConfig.fields
     * @see AsanaConfig.expandedResponses Overrides `fields` supplied to return all data associated with each task
     *                                    (not recommended – only for debugging purposes)
     */
    fun Project.getCustomFields(): Collection<CustomField> {
        return requestExecutor.projects.getCustomFieldSettingsPaginated(this).map { it.customField }
    }

    /**
     * Returns a `List` of all the new tasks on this project, with or without attachments based on the
     * [includeAttachments] flag.
     *
     * New tasks are those that were added since the last time this function was called. However, this is only reliable
     * if calls are made more frequently than every 24 hours; this is due to a limitation of the Asana API.
     * [For more details, please see this section of Asana's documentation](https://developers.asana.com/docs/events).
     *
     * Also, please note that this is an *expensive* function, as it calls the [get] extension function defined in this
     * class on each new [Task]. Unless you really need the complete record of each task, use the [pollEventStreamLazy]
     * function.
     *
     * @see pollEventStream
     * @see pollEventStreamLazy
     */
    fun Project.getNewTasks(includeAttachments: Boolean = false): List<Task> {
        return pollEventStream(includeAttachments, Action.ADDED)
    }

    /**
     * Returns a `Set` of [Event] objects representing all new tasks on this project.
     *
     * New tasks are those that were added since the last time this function was called. However, this is only reliable
     * if calls are made more frequently than every 24 hours; this is due to a limitation of the Asana API.
     * [For more details, please see this section of Asana's documentation](https://developers.asana.com/docs/events).
     *
     * @see pollEventStream
     * @see pollEventStreamLazy
     */
    fun Project.getNewTasksLazy(): Set<Event> {
        return pollEventStreamLazy(Action.ADDED)
    }

    /**
     * Polls this project for task events that match the given [actions], returning a `List` of tasks passing the
     * filter, with or without attachments based on the [includeAttachments] flag.
     *
     * If a reliable data stream is needed, make calls to this function more frequently than every 24 hours.
     * [For more details, please see this section of Asana's documentation](https://developers.asana.com/docs/events).
     *
     * Also, please note that this is an *expensive* function, as it calls the [get] extension function defined in this
     * class on each new [Task]. Unless you really need the complete record of each task, use the [pollEventStreamLazy]
     * function.
     *
     * @see pollEventStreamLazy
     */
    fun Project.pollEventStream(includeAttachments: Boolean = false, vararg actions: Action): List<Task> {
        return pollEventStreamLazy(*actions)
            .asSequence()
            .map(Event::resourceGid)
            .map { task(it) }
            .map { it.get(includeAttachments) }
            .toList()
    }

    /**
     * Polls this project for task events that match the given [actions], returning a `Set` containing the unique
     * identifiers of all tasks passing the filter.
     *
     * If a reliable data stream is needed, make calls to this function more frequently than every 24 hours.
     * [For more details, please see this section of Asana's documentation](https://developers.asana.com/docs/events).
     *
     * @see pollEventStream
     */
    fun Project.pollEventStreamLazy(vararg actions: Action): Set<Event> {
        return requestExecutor.events.getTaskEvents(this, *actions)
    }

    /**
     * Returns a count of the tasks associated with this project.
     */
    fun Project.getTaskCount(): Int {
        return requestExecutor.projects.getTaskCount(this)
    }

// Workspace extension functions

    /**
     * Searches the receiving `Workspace`, returning a list of compact tasks that match the given filters.
     *
     * Supported parameters [are outlined at this link](https://developers.asana.com/docs/search-tasks-in-a-workspace).
     * Below is an example call:
     *
     * ```kotlin
     * val result: List<Task> = asanaContext {
     *     workspace("54321").search(
     *         "text" to "Hello World!",
     *         "created_at.after" to LocalDate.now(),
     *         "projects.any" to "12345"
     *     )
     * }
     * ```
     */
    fun Workspace.search(vararg filters: Pair<String, Any>): List<Task> {
        return requestExecutor.workspaces.searchWorkspacePaginated(this, filters)
    }

    /**
     * Returns all projects within this workspace, optionally filtered by their archived status with the
     * [includeArchived] flag.
     *
     * Projects returned by this function have the fields supplied by this [AsanaClientExtension]'s `config.fields`
     * setting.
     *
     * @see AsanaConfig.fields
     * @see AsanaConfig.expandedResponses Overrides `fields` supplied to return all data associated with each task
     *                                    (not recommended – only for debugging purposes)
     */
    fun Workspace.getAllProjects(includeArchived: Boolean = true): Collection<Project> {
        return requestExecutor.workspaces.getProjectsPaginated(this, includeArchived)
    }

    /**
     * Instantiates a project template (given by the [templateProjectGid]) with the given [projectTitle] onto the given
     * [projectTeam], returning the resultant async [Job] for the request.
     *
     * Note that the supplied `projectTeam` must be the same team the project template is homed on.
     */
    fun Workspace.instantiateTemplate(templateProjectGid: String, projectTitle: String, projectTeam: String): Job {
        return requestExecutor.workspaces.instantiateTemplate(templateProjectGid, projectTitle, projectTeam)
    }

    /**
     * Returns a collection of all [CustomField] objects associated with this [Workspace].
     *
     * `CustomField` objects returned by this function have the fields supplied by this [AsanaClientExtension]'s `
     * config.fields` setting.
     *
     * @see AsanaConfig.fields
     * @see AsanaConfig.expandedResponses Overrides `fields` supplied to return all data associated with each task
     *                                    (not recommended – only for debugging purposes)
     */
    fun Workspace.getCustomFields(): Collection<CustomField> {
        return requestExecutor.workspaces.getCustomFieldsPaginated(this)
    }

    /**
     * Returns an Asana [Workspace] with the given [workspaceGid] (globally unique identifier).
     */
    fun workspace(workspaceGid: String): Workspace = Workspace().initResource(workspaceGid)

    /**
     * Returns the result of calling the given [block] on an Asana [Workspace] with the given [workspaceGid].
     */
    inline fun <R> workspace(workspaceGid: String, block: Workspace.() -> R): R = workspace(workspaceGid).block()

// Workspace extension functions

    /**
     * Returns an Asana [Portfolio] with the given [portfolio] (globally unique identifier).
     */
    fun portfolio(portfolio: String): Portfolio = Portfolio().initResource(portfolio)

    /**
     * Returns the result of calling the given [block] on an Asana [Portfolio] with the given [portfolioGid].
     */
    inline fun <R> portfolio(portfolioGid: String, block: Portfolio.() -> R): R = portfolio(portfolioGid).block()

    /**
     * Returns all items contained within the receiver [Portfolio].
     */
    fun Portfolio.getItems(): List<Project> {
        return requestExecutor.portfolios.getItemsPaginated(this)
    }

// Type conversion functions

    /**
     * Returns the result of converting this [Task] to an object of the given [toClass], optionally applying the given
     * [runAfterConverting] function.
     */
    fun <R : AsanaSerializable<R>> Task.convertTo(
        toClass: KClass<out R>,
        runAfterConverting: (source: Task, destination: R) -> Unit = { _, _ -> }
    ): R {
        checkConfigFieldsForSerializing()
        return AsanaTaskSerializer(toClass, getContextFor(this))
            .deserialize(this, runAfterConverting)
    }

    /**
     * Returns the result of converting this [AsanaSerializable] to a [Task] with the given resource's [context]
     * (such as a [Project] or [Workspace]).
     *
     * It's recommended that you call this function within the context of a resource. For example:
     * ```kotlin
     *    asanaContext {
     *        project("12345") { // this = an instance of project
     *            val person: Person = ...
     *            val task: Task = person.convertToTask(this)   // <- context = this = this@project
     *        }
     *    }
     * ```
     */
    fun <R : AsanaSerializable<R>> R.convertToTask(
        context: Resource,
        runAfterConverting: (source: R, destination: Task) -> Unit = { _, _ -> }
    ): Task {
        checkConfigFieldsForSerializing()
        return AsanaTaskSerializer(this::class, getContextFor(context))
            .serialize(this, runAfterConverting)
    }

    /**
     * Converts all Tasks belonging to this project into objects of the given [toClass], then applies the given
     * [runAfterConverting] function, then finally returns the [List] result.
     *
     * Example usage:
     * ```kotlin
     *    val people: List<People> = asanaContext {
     *        project("12345").convertTasksToListOf(Person::class)
     *    }
     * ```
     */
    fun <R : AsanaSerializable<R>> Project.convertTasksToListOf(
        toClass: KClass<out R>,
        includeAttachments: Boolean = false,
        runAfterConverting: (source: Task, destination: R) -> Unit = { _, _ -> }
    ): List<R> {
        checkConfigFieldsForSerializing()
        return this
            .getTasks(includeAttachments)
            .convertToListOf(toClass, this, runAfterConverting)
    }

    /**
     * Returns the result of converting this `List` of [AsanaSerializable] objects into a `List` of [Task] objects,
     * using the given [context] resource (such as `Project` or `Workspace`).
     *
     * It's recommended that you call this function within the context of a resource. For example:
     * ```kotlin
     *    asanaContext {
     *        project("12345") { // this = an instance of project
     *            val people: List<Person> = ...
     *            val tasks: List<Task> = people.convertToTaskList(this)   // <- context = this: Project
     *        }
     *    }
     * ```
     */
    fun <R : AsanaSerializable<R>> List<R>.convertToTaskList(
        context: Resource,
        runAfterConverting: (source: R, destination: Task) -> Unit = { _, _ -> }
    ): List<Task> {
        checkConfigFieldsForSerializing()
        if (isEmpty()) return emptyList()
        val converter = AsanaTaskSerializer(this[0]::class, getContextFor(context))
        return this.map { converter.serialize(it, runAfterConverting) }
    }

    /**
     * Converts each `Task` in this `List<Task>` into objects of the given [toClass] within the context of the given
     * [resource] (such as `Task`, `Project`, or `Workspace`), then applies the [runAfterConverting] function, then
     * finally returns the [List] result.
     */
    fun <R : AsanaSerializable<R>> List<Task>.convertToListOf(
        toClass: KClass<out R>,
        resource: Resource,
        runAfterConverting: (source: Task, destination: R) -> Unit = { _, _ -> }
    ): List<R> {
        if (isEmpty()) return emptyList()
        val converter = AsanaTaskSerializer(toClass, getContextFor(resource))
        return map { converter.deserialize(it, runAfterConverting) }
    }

// Internal helper functions

    /**
     * Detects and returns the [CustomFieldContext] for the given [resource]. If no context exists within this
     * [AsanaClientExtension], this function creates a new one and caches it.
     *
     * @throws CustomFieldException if the given resource is not a `Task`, `Project`, or `Workspace`
     */
    private fun getContextFor(resource: Resource): CustomFieldContext {
        val existingContext = contextFor(resource)
        if (existingContext != null) return existingContext

        val newContext = when (resource) {
            is Task -> TaskCustomFieldContext(resource)
            is Project -> ProjectCustomFieldContext(resource, this)
            is Workspace -> WorkspaceCustomFieldContext(resource, this)
            else -> throw CustomFieldException(
                """
                The given resource [$resource] cannot be used to serialize tasks and data objects.
                You may only supply Task, Project, or Workspace objects.
            """.trimIndent()
            )
        }
        contexts[resource.gid] = newContext

        return newContext
    }

    /**
     * Returns a nullable [CustomFieldContext] for the given [resource].
     */
    private fun contextFor(resource: Resource): CustomFieldContext? = contexts[resource.gid]

    /**
     * Guard against (de)serialization attempts when `custom_fields` option is not supplied.
     */
    private fun checkConfigFieldsForSerializing() {
        if ("custom_fields" !in config.fields) throw CustomFieldException(
            """
            
            Cannot serialize with the specified: AsanaConfig.fields=${config.fields.contentToString()}
            
            Hint: if you're serializing a data object into tasks (or vice-versa), you must specify at least the 
            "custom_fields" option when constructing your AsanaConfig object.
            
        """.trimIndent()
        )
    }

    /**
     * Returns an Asana resource with the given [gid] (globally unique identifier).
     */
    private fun <T : Resource> T.initResource(gid: String): T = apply { this.gid = gid }

}
