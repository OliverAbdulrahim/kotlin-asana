package org.conservationco.asana

/**
 * Returns the result of applying the given [block] to the given [clientExtension].
 *
 * Use this function to switch into the context of an [AsanaClientExtension]. The default [clientExtension] is
 * instantiated with the `asana_access_token` environment variable. For example:
 *
 * ```kotlin
 * asanaContext {
 *     // Tasks
 *     val person: Person = task("123").convertTo(Person::class)
 *
 *     // Projects
 *     val taskAgain: Task = project("456") { person.convertToTask(this).update() }
 *
 *     // Workspaces
 *     val search: List<Task> = workspace("789").search("ice cream sundae", "456")
 * }
 * ```
 *
 *  Optionally, pass in your own client:
 * ```kotlin
 *     val client = com.asana.Client(...)
 *     val config = AsanaConfig(client, verboseLogs = ..., expandedResponses = ..., fields = arrayOf("...", "..."))
 *     val ext = AsanaClientExtension(config)
 *     asanaContext(ext) { ... }
 * ```
 *
 * @see AsanaConfig
 * @see AsanaClientExtension
 */
inline fun <R> asanaContext(
    clientExtension: AsanaClientExtension = ClientDefaults.CLIENT_EXT,
    block: AsanaClientExtension.() -> R
): R = clientExtension.block()

/**
 * Returns the result of applying the given [block] to a new [AsanaClientExtension] with the given [config].
 *
 * Use this function to switch into the context of an [AsanaClientExtension].
 *
 * @see asanaContext
 */
inline fun <R> asanaContext(
    config: AsanaConfig,
    block: AsanaClientExtension.() -> R
): R = asanaContext(AsanaClientExtension(config), block)
