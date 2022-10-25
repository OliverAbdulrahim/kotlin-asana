package org.conservationco.asana.serialization.customfield.context

import com.asana.models.CustomField

object NoOpCustomFieldContext : CustomFieldContext() {
    override fun loadCustomFields(): Map<String, CustomField> = emptyMap()
}
