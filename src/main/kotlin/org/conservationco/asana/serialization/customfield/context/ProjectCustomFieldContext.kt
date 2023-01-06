package org.conservationco.asana.serialization.customfield.context

import com.asana.models.CustomField
import com.asana.models.Project
import org.conservationco.asana.asanaContext
import org.conservationco.asana.extensions.AsanaClientExtension

class ProjectCustomFieldContext(
    private val project: Project,
    private val client: AsanaClientExtension,
) : CustomFieldContext() {
    override fun loadCustomFields(): Map<String, CustomField> = asanaContext(client) {
        project.getCustomFields().mapGidsToCustomFields()
    }
}
