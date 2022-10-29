package org.conservationco.asana.serialization.customfield

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class AsanaCustomField(val name: String)
