package org.conservationco.asana.serialization

import com.asana.models.Resource
import com.asana.models.Task

/**
 * @param <S> The type of the source object, such as [com.asana.models.Task] or [com.asana.models.Project].
 * @param <D> The type of the destination object.
 */
interface AsanaSerializer<A : Resource, B : AsanaSerializable<B>> {
    fun serialize(source: B): A
    fun serialize(source: B, runAfter: (source: B, destination: A) -> Unit): A
    fun deserialize(source: A, runAfter: (source: A, destination: B) -> Unit): B
    fun deserialize(source: A): B
}
