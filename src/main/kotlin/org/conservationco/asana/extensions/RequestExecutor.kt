package org.conservationco.asana.extensions

import org.conservationco.asana.AsanaConfig
import org.conservationco.asana.extensions.events.Events
import org.conservationco.asana.extensions.portfolios.Portfolios
import org.conservationco.asana.extensions.projects.Projects
import org.conservationco.asana.extensions.tasks.Tasks
import org.conservationco.asana.extensions.workpsaces.Workspaces

/**
 * Utility class for executing Asana requests.
 *
 * @property config Configuration for this object, including what client to select, verbosity of logs, and optional
 *                  fields to return.
 */
class RequestExecutor(
    private val config: AsanaConfig,
) {

    val tasks = Tasks(config)
    val projects = Projects(config)
    val workspaces = Workspaces(config)
    val events = Events(config)
    val portfolios = Portfolios(config)

}
