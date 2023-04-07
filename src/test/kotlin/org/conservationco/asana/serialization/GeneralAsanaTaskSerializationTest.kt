package org.conservationco.asana.serialization

import com.asana.models.CustomField
import com.asana.models.Task
import org.conservationco.asana.serialization.stubs.*
import org.conservationco.asana.serialization.stubs.PersonCustomFields.noOpGid
import org.conservationco.asana.serialization.stubs.PersonCustomFields.personEnumCustomField
import org.conservationco.asana.serialization.stubs.PersonCustomFields.personMultiEnumField
import org.conservationco.asana.serialization.stubs.PersonCustomFields.personTextCustomField
import org.conservationco.asana.util.enumOptionOf
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class GeneralAsanaTaskSerializationTest {

    private val underTest = AsanaTaskSerializer(Person::class, PersonCustomFieldContextStub())

    @Test
    fun `serialization of text CustomField`() {
        // Given
        val source = Person(favoriteDessert = "Pie")
        val destination: Task = underTest.serialize(source)

        // When
        val expectedCustomField = personTextCustomField()
        val actualCustomField = destination.customFields.find { it.name == expectedCustomField.name }!!

        // Then
        assertForCustomFields(
            expectedCustomField, source.favoriteDessert, actualCustomField,
            actualCustomField.textValue
        )
    }

    @Test
    fun `serialization of enum CustomField`() {
        // Given
        val source = Person(favoriteSeason = "Winter")
        val destination: Task = underTest.serialize(source)

        // When
        val expectedCustomField = personEnumCustomField()
        val actualCustomField = destination.customFields.find { it.name == expectedCustomField.name }!!

        // Then
        assertForCustomFields(
            expectedCustomField,
            source.favoriteSeason,
            actualCustomField,
            actualCustomField.enumValue?.name
        )
    }

    @Test
    fun `serialization of multi_enum CustomField`() {
        // Given
        val source = Person(languagesSpoken = arrayOf("Turkish", "Spanish"))
        val destination: Task = underTest.serialize(source)

        // When
        val expectedCustomField = personMultiEnumField()
        val actualCustomField = destination.customFields.find { it.name == expectedCustomField.name }!!

        // Then
        assertForCustomFields(
            expectedCustomField,
            source.languagesSpoken,
            actualCustomField,
            actualCustomField.multiEnumValues.map { it?.name }.toTypedArray()
        )
    }

    @Test
    fun `deserialization of text CustomField`() {
        // Given
        val textCustomField = personTextCustomField().apply { textValue = "Nan-e nokhodchi"}
        val source = Task().apply { customFields = listOf(textCustomField) }
        val destination: Person = underTest.deserialize(source)

        // When
        val expectedValue = textCustomField.textValue
        val actualValue = destination.favoriteDessert

        // Then
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `deserialization of enum CustomField`() {
        // Given
        val enumCustomField = personEnumCustomField().apply { enumValue = enumOptionOf("Winter", noOpGid) }
        val source = Task().apply { customFields = listOf(enumCustomField) }
        val destination: Person = underTest.deserialize(source)

        // When
        val expectedValue = enumCustomField.enumValue.name
        val actualValue = destination.favoriteSeason

        // Then
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `deserialization of multi_enum CustomField`() {
        // Given
        val multiEnumCustomField = personMultiEnumField().apply {
            multiEnumValues = listOf(
                enumOptionOf("Turkish", noOpGid),
                enumOptionOf("Spanish", noOpGid))
        }
        val source = Task().apply { customFields = listOf(multiEnumCustomField) }
        val destination: Person = underTest.deserialize(source)

        // When
        val expectedValue = multiEnumCustomField.multiEnumValues.map { it.name }.toTypedArray()
        val actualValue = destination.languagesSpoken

        // Then
        assertContentEquals(expectedValue, actualValue)
    }

    @Test
    fun `deserialization of Task with custom logic running after`() {
        // Given
        val task = Task().apply { name = "Somebody that I used to know" }
        val person = underTest.deserialize(task) { source, destination ->  destination.name = source.name }

        // When
        val expectedName = task.name
        val actualName = person.name

        // Then
        assertEquals(expectedName, actualName)
    }

    @Test
    fun `serialization of Task with custom logic running after`() {
        // Given
        val person = Person(id = "2147483648")
        val task = underTest.serialize(person) { source, destination -> destination.gid = source.id  }

        // When
        val expectedGid = person.id
        val actualGid = task.gid

        // Then
        assertEquals(expectedGid, actualGid)
    }

    private fun <T> assertForCustomFields(
        expectedCustomField: CustomField,
        expectedCustomFieldValue: T,
        actualCustomField: CustomField,
        actualCustomFieldValue: T
    ) {
        val expectedCustomFieldGid = expectedCustomField.gid
        val expectedCustomFieldName = expectedCustomField.name

        val actualCustomFieldGid = actualCustomField.gid
        val actualCustomFieldName = actualCustomField.name

        assertEquals(expectedCustomFieldGid, actualCustomFieldGid)
        assertEquals(expectedCustomFieldName, actualCustomFieldName)
        if (expectedCustomFieldValue is Array<*>) {
            @Suppress("UNCHECKED_CAST")
            assertContentEquals(expectedCustomFieldValue as Array<T>?, actualCustomFieldValue as Array<T>?)
        }
        else assertEquals(expectedCustomFieldValue, actualCustomFieldValue)
    }

}
