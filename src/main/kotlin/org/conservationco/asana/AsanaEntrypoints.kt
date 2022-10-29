package org.conservationco.asana

import com.asana.models.Project
import com.asana.models.Resource
import com.asana.models.Task
import com.asana.models.Workspace

fun task(taskGid: String): Task = Task().initResource(taskGid)

fun project(projectGid: String): Project = Project().initResource(projectGid)

inline fun <R> project(gid: String, block: Project.() -> R): R = project(gid).block()

fun workspace(workspaceGid: String): Workspace = Workspace().initResource(workspaceGid)

inline fun <R> workspace(gid: String, block: Workspace.() -> R): R = workspace(gid).block()

private fun <T : Resource> T.initResource(gid: String): T = apply { this.gid = gid }

inline fun <R> asanaContext(
    client: AsanaClientExtension = ClientDefaults.CLIENT_EXT,
    block: AsanaClientExtension.() -> R
): R = client.block()

inline fun <R> asanaContext(
    config: AsanaConfig,
    block: AsanaClientExtension.() -> R
): R = AsanaClientExtension(config).block()
