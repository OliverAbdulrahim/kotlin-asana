package org.conservationco.asana.customfield.context

import com.asana.models.CustomField
import org.conservationco.asana.asanaContext
import org.conservationco.asana.util.selectProject

class ProjectCustomFieldContext(private val gid: String) : CustomFieldContext() {
    override fun loadCustomFields(): Map<String, CustomField> {
        asanaContext { return selectProject(gid).getCustomFields().convertToMap() }
    }
}
