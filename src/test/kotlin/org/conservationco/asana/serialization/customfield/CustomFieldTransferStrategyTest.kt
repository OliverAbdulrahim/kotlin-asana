package org.conservationco.asana.serialization.customfield

import org.conservationco.asana.serialization.stubs.TestTransferClasses.*
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.test.assertEquals

internal class CustomFieldTransferStrategyTest {

    @Test
    fun `create transfer strategy for a class without properties`() {
        assertForPropertiesOfGivenClass(ClassWithNoProperties::class)
    }

    @Test
    fun `create transfer strategy for a class with no serializable properties`() {
        assertForPropertiesOfGivenClass(ClassWithNoSerializableProperties::class)
    }

    @Test
    fun `create transfer strategy for a class with serializable properties`() {
        assertForPropertiesOfGivenClass(ClassWithSerializableProperties::class)
    }

    @Test
    fun `create transfer strategy for a class with inherited serializable properties`() {
        assertForPropertiesOfGivenClass(ClassWithInheritedSerializableProperties::class)
    }

    @Test
    fun `create transfer strategy for a class with both serializable and non-serializable properties`() {
        assertForPropertiesOfGivenClass(ClassWithBothSerializableAndNonSerializableProperties::class)
    }

    private fun assertForPropertiesOfGivenClass(clazz: KClass<out Any>) {
        // Given
        val underTest = CustomFieldTransferStrategy(clazz)

        // When
        val expectedProperties = clazz.memberProperties.filter { it.hasAnnotation<AsanaCustomField>() }
        val expectedSerializedPropertyNames = expectedProperties.map { it.findAnnotation<AsanaCustomField>()?.name }
        val actualProperties = underTest.properties.values
        val actualSerializedPropertyNames = underTest.properties.keys

        // Then
        assertEquals(expectedProperties.size, actualProperties.size)
        assert(actualProperties.containsAll(expectedProperties))
        assertEquals(expectedSerializedPropertyNames.size, actualSerializedPropertyNames.size)
        assert(actualSerializedPropertyNames.containsAll(expectedSerializedPropertyNames))
    }

}
