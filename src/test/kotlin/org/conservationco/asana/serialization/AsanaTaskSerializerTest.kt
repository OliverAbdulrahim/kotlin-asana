package org.conservationco.asana.serialization

import com.asana.models.CustomField
import com.asana.models.Task
import org.conservationco.asana.serialization.stubs.*
import org.conservationco.asana.serialization.stubs.PersonCustomFieldContextStub
import org.conservationco.asana.serialization.stubs.getEnumCustomField
import org.conservationco.asana.serialization.stubs.getMultiEnumField
import org.conservationco.asana.serialization.stubs.getTextCustomField
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class AsanaTaskSerializerTest {

    private val underTest = AsanaTaskSerializer(Person::class, PersonCustomFieldContextStub())

    @Test
    fun `serialization of text CustomField`() {
        // Given
        val source = Person(favoriteDessert = "Pie")
        val destination: Task = underTest.serialize(source)

        // When
        val expectedCustomField = getTextCustomField()
        val actualCustomField = destination.customFields.find { it.name == expectedCustomField.name }!!

        // Then
        assertForCustomFields(
            expectedCustomField = expectedCustomField,
            expectedCustomFieldValue = source.favoriteDessert,
            actualCustomField = actualCustomField,
            actualCustomFieldValue = actualCustomField.textValue
        )
    }

    @Test
    fun `serialization of enum CustomField`() {
        // Given
        val source = Person(favoriteSeason = "Winter")
        val destination: Task = underTest.serialize(source)

        // When
        val expectedCustomField = getEnumCustomField()
        val actualCustomField = destination.customFields.find { it.name == expectedCustomField.name }!!

        // Then
        assertForCustomFields(
            expectedCustomField = expectedCustomField,
            expectedCustomFieldValue = source.favoriteSeason,
            actualCustomField = actualCustomField,
            actualCustomFieldValue = actualCustomField.enumValue?.name
        )
    }

    @Test
    fun `serialization of multi_enum CustomField`() {
        // Given
        val source = Person(languagesSpoken = arrayOf("Turkish", "Spanish"))
        val destination: Task = underTest.serialize(source)

        // When
        val expectedCustomField = getMultiEnumField()
        val actualCustomField = destination.customFields.find { it.name == expectedCustomField.name }!!

        // Then
        assertForCustomFields(
            expectedCustomField = expectedCustomField,
            expectedCustomFieldValue = source.languagesSpoken,
            actualCustomField = actualCustomField,
            actualCustomFieldValue = actualCustomField.multiEnumValues.map { it?.name }.toTypedArray()
        )
    }

    @Suppress("UNCHECKED_CAST")
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

        assertEquals(expected = expectedCustomFieldGid, actual = actualCustomFieldGid)
        assertEquals(expected = expectedCustomFieldName, actual = actualCustomFieldName)
        if (expectedCustomFieldValue is Array<*>) {
            assertContentEquals(
                expected = expectedCustomFieldValue as Array<T>?,
                actual = actualCustomFieldValue as Array<T>?
            )
        }
        else assertEquals(expected = expectedCustomFieldValue, actual = actualCustomFieldValue)
    }

}
