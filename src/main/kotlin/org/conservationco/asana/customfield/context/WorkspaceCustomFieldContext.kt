package org.conservationco.asana.customfield.context

import com.asana.models.CustomField
import org.conservationco.asana.asanaContext
import org.conservationco.asana.util.selectWorkspace

class WorkspaceCustomFieldContext(private val gid: String) : CustomFieldContext() {
    override fun loadCustomFields(): Map<String, CustomField> {
        asanaContext { return selectWorkspace(gid).getCustomFields().convertToMap() }
    }
}
