package org.conservationco.asana.serialization

/**
 * @param T The type of the serializable object.
 */
interface AsanaSerializable<T : AsanaSerializable<T>> {
    var id: String
    var name: String
}
