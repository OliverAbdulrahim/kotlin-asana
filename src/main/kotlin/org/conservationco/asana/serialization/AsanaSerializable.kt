package org.conservationco.asana.serialization

/**
 * Interface for classes that are (de)serializable from Asana resources.
 *
 * @param T The type of the serializable object.
 */
interface AsanaSerializable<T : AsanaSerializable<T>> {
    var id: String
    var name: String
}
