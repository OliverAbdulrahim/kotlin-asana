package org.conservationco.asana.customfield.context

import com.asana.models.CustomField
import org.conservationco.asana.util.asanaContext
import org.conservationco.asana.util.selectProject

class ProjectCustomFieldContext(override val gid: String) : CustomFieldContext(gid) {
    override fun loadCustomFields(): Map<String, CustomField> {
        asanaContext { return selectProject(gid).getCustomFields().convertToMap() }
    }
}
