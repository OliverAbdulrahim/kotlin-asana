package org.conservationco.asana.serialization

import com.asana.models.Resource

/**
 * @param <S> The type of the source object, such as [com.asana.models.Task] or [com.asana.models.Project].
 * @param <D> The type of the destination object.
 */
interface AsanaSerializer<A : Resource, B : AsanaSerializable<B>> {
    fun serialize(source: B, alsoApply: A.(source: B) -> A): A
    fun deserialize(source: A, alsoApply: B.(source: A) -> B): B
}
