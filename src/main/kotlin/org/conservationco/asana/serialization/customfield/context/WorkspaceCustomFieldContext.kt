package org.conservationco.asana.serialization.customfield.context

import com.asana.models.CustomField
import com.asana.models.Workspace
import org.conservationco.asana.asanaContext
import org.conservationco.asana.extensions.AsanaClientExtension

class WorkspaceCustomFieldContext(
    private val workspace: Workspace,
    private val client: AsanaClientExtension,
) : CustomFieldContext() {
    override fun loadCustomFields(): Map<String, CustomField> = asanaContext(client) {
        workspace.getCustomFields().mapGidsToCustomFields()
    }
}
