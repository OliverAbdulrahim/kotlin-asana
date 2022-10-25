package org.conservationco.asana.customfield.context

import com.asana.models.CustomField
import com.asana.models.Task

class TaskCustomFieldContext(private val task: Task) : CustomFieldContext() {
    override fun loadCustomFields(): Map<String, CustomField> = task.customFields.convertToMap()
}
