package org.conservationco.asana.serialization.customfield

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class AsanaCustomField(val name: String)
