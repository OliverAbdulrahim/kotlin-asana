package org.conservationco.asana.util

import com.asana.models.*
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext

// Resource selection functions

fun selectProject(projectGid: String): Project = Project().initResource(projectGid)

fun selectWorkspace(workspaceGid: String): Workspace = Workspace().initResource(workspaceGid)

fun selectTask(taskGid: String): Task = Task().initResource(taskGid)

private fun <T : Resource> T.initResource(gid: String): T = apply { this.gid = gid }

// Collection functions

fun Collection<CustomField>.mapGidsToValues(context: CustomFieldContext): Map<String, Any?> =
    associateBy({ it.gid }, { it.inferValue(context) })

fun Collection<Resource>.toGidArray(): Array<String> = map { it.gid }.toTypedArray()
