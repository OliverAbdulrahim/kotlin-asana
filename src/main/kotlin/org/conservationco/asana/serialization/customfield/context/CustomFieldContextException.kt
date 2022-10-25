package org.conservationco.asana.serialization.customfield.context

import java.lang.IllegalArgumentException

class CustomFieldContextException(override val message: String) : IllegalArgumentException(message)
