package org.conservationco.asana.serialization.stubs

import org.conservationco.asana.serialization.customfield.AsanaCustomField

internal class TestTransferClasses {

    internal class ClassWithNoProperties

    internal class ClassWithNoSerializableProperties(
        val skippedProperty: String,
    )

    internal open class ClassWithSerializableProperties(
        @AsanaCustomField("Some property")
        val someProperty: String,
    )

    internal class ClassWithInheritedSerializableProperties : ClassWithSerializableProperties("A value")

    internal class ClassWithBothSerializableAndNonSerializableProperties(
        @AsanaCustomField("Some property")
        val someProperty: String,
        val skippedProperty: String,
    )

}
