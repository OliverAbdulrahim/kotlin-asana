package org.conservationco.asana.util

import com.asana.models.*
import org.conservationco.asana.AsanaClient
import org.conservationco.asana.AsanaConfig
import org.conservationco.asana.customfield.getValue

// asanaContext entrypoint

inline fun asanaContext(config: AsanaConfig = AsanaConfig(), block: AsanaClient.() -> Unit): AsanaClient = AsanaClient(config).apply(block)

// Resource selection functions

fun selectProject(projectGid: String): Project = Project().initResource(projectGid)

fun selectWorkspace(workspaceGid: String): Workspace = Workspace().initResource(workspaceGid)

fun selectTask(taskGid: String): Task = Task().initResource(taskGid)

private fun <T : Resource> T.initResource(gid: String): T = apply { this.gid = gid }

// Collection functions

fun Collection<CustomField>.convertGidsToValues(): Map<String, Any?> = associateBy({ it.gid }, { it.getValue() })

fun Collection<Resource>.gidsForResourceCollection(): Array<String> = map { it.gid }.toTypedArray()

// Task extension functions

fun Task.selectMultiEnumOptions(customFieldName: String, vararg optionsToSelect: String) {
    val customField = this.findCustomField(customFieldName)
    customField!!.multiEnumValues = customField
        .enumOptions
        .filter { optionsToSelect.contains(it.name) }
}

fun Task.findCustomField(customFieldName: String): CustomField? = customFields.find { it.name.contains(customFieldName)
}

fun Task.modTimeDiffers(other: Task): Boolean = modifiedAt != other.modifiedAt
