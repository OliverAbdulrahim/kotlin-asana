package org.conservationco.asana.customfield.context

import java.lang.IllegalArgumentException

class CustomFieldContextException(override val message: String) : IllegalArgumentException(message)
