package org.conservationco.asana.customfield


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class CustomFieldSerializable(
    val name: String,
)
