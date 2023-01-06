package org.conservationco.asana.serialization

import com.asana.models.Project
import com.asana.models.Resource
import com.asana.models.Task

/**
 * Interface for classes that (de)serialize Asana resources.
 *
 * @param A The type of the Asana object, such as [Task] or [Project].
 * @param B The type of the data class object.
 */
interface AsanaSerializer<A : Resource, B : AsanaSerializable<B>> {

    /**
     * Converts the given [source] ([AsanaSerializable]) object into the [Resource] this object serializes, returning
     * the serialized result.
     */
    fun serialize(source: B): A

    /**
     * Converts the given [source] ([AsanaSerializable]) object into the [Resource] this object serializes, then applies
     * the given [runAfter] function, and finally returns the serialized result.
     */
    fun serialize(source: B, runAfter: (source: B, destination: A) -> Unit): A

    /**
     * Converts the given [source] ([Resource]) object into the [AsanaSerializable] that this object serializes,
     * returning the deserialized result.
     */
    fun deserialize(source: A): B

    /**
     * Converts the given [source] ([Resource]) object into the [AsanaSerializable] that this object serializes, then
     * applies the given [runAfter] function, and finally returns the deserialized result.
     */
    fun deserialize(source: A, runAfter: (source: A, destination: B) -> Unit): B

}
