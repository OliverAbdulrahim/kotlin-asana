package org.conservationco.asana.customfield.context

import com.asana.models.CustomField

class NoOpCustomFieldContext : CustomFieldContext("") {
    override fun loadCustomFields(): Map<String, CustomField> = emptyMap()
}
