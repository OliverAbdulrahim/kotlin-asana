package org.conservationco.asana.serialization

import com.asana.models.Task
import org.conservationco.asana.exception.CustomFieldException
import org.conservationco.asana.serialization.stubs.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class OptionalAsanaTaskSerializerTest {

    @Test
    fun `serialize and deserialize when required and optional fields are present in context`() {
        // Given
        val underTest = AsanaTaskSerializer(`Serializable Class`::class, `Context With All Fields`())

        // When
        val source = `Serializable Class`(
            presentRequiredField = "first",
            presentOptionalField = "second",
            absentRequiredField = "third",
            absentOptionalField = "fourth",
        )

        // Then
        val result: Task = assertDoesNotThrow { underTest.serialize(source) }
        val andBack: `Serializable Class` = assertDoesNotThrow { underTest.deserialize(result) }

        assertEquals(source, andBack)
    }

    @Test
    fun `serialize and deserialize when all required fields are present in context`() {
        // Given
        val underTest = AsanaTaskSerializer(`Serializable Class`::class, `Context With Optional Fields Missing`())

        // When
        val source = `Serializable Class`(
            presentRequiredField = "first",
            presentOptionalField = "second",
            absentRequiredField = "third",
            absentOptionalField = "fourth",
        )

        // Then
        val result: Task = assertDoesNotThrow { underTest.serialize(source) }
        val andBack: `Serializable Class` = assertDoesNotThrow { underTest.deserialize(result) }

        assertEquals(source.presentRequiredField, andBack.presentRequiredField)
        assertEquals(source.absentRequiredField, andBack.absentRequiredField)
        assertNotEquals(source.presentOptionalField, andBack.presentOptionalField)
        assertNotEquals(source.absentOptionalField, andBack.absentOptionalField)
    }

    @Test
    fun `serialize when required fields are absent in context`() {
        // Given
        val underTest = AsanaTaskSerializer(`Serializable Class`::class, `Context With Required Fields Missing`())

        // When
        val source = `Serializable Class`(
            presentRequiredField = "first",
            presentOptionalField = "second",
            absentRequiredField = "third",
            absentOptionalField = "fourth",
        )

        // Then
        assertThrows<CustomFieldException> { underTest.serialize(source) }
    }

}
