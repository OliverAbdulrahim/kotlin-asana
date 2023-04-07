package org.conservationco.asana.serialization.stubs

import org.conservationco.asana.serialization.AsanaSerializable
import org.conservationco.asana.serialization.customfield.AsanaCustomField
import org.junit.jupiter.api.Test

internal class `Serializable Class`(

    // Expect that this is serialized normally
    @AsanaCustomField("Present required field")
    var presentRequiredField: String = "",

    // Expect that this is serialized normally
    @AsanaCustomField("Present optional field", true)
    var presentOptionalField: String = "",

    // Expect that an error is thrown when serializing if the CustomFieldContext doesn't have a matching field
    @AsanaCustomField("Absent required field")
    var absentRequiredField: String = "",

    // Expect that this is just skipped when serializing if the CustomFieldContext doesn't have a matching field
    @AsanaCustomField("Absent optional field", true)
    var absentOptionalField: String = "",

) : AsanaSerializable<`Serializable Class`> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is `Serializable Class`) return false

        if (presentRequiredField != other.presentRequiredField) return false
        if (presentOptionalField != other.presentOptionalField) return false
        if (absentRequiredField != other.absentRequiredField) return false
        return absentOptionalField == other.absentOptionalField
    }

    override fun hashCode(): Int {
        var result = presentRequiredField.hashCode()
        result = 31 * result + presentOptionalField.hashCode()
        result = 31 * result + absentRequiredField.hashCode()
        result = 31 * result + absentOptionalField.hashCode()
        return result
    }

    override fun toString(): String {
        return "`Serializable Class`(" +
                "presentRequiredField='$presentRequiredField', " +
                "presentOptionalField='$presentOptionalField', " +
                "absentRequiredField='$absentRequiredField', " +
                "absentOptionalField='$absentOptionalField')"
    }

}
