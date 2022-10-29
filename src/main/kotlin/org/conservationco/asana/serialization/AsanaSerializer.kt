package org.conservationco.asana.serialization

import com.asana.models.Resource

/**
 * @param <S> The type of the source object, such as [com.asana.models.Task] or [com.asana.models.Project].
 * @param <D> The type of the destination object.
 */
interface AsanaSerializer<A : Resource, B : AsanaSerializable<B>> {
    fun serialize(source: B): A
    fun deserialize(source: A): B
}
