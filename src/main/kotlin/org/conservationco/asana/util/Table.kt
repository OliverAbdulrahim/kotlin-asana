package org.conservationco.asana.util

/**
 * A `Table` represents an arbitrary data store over REST with create, read, update, and delete capabilities.
 *
 * @param K The type of object that serves as the primary key for entries in this `Table`.
 * @param V The type of object that encapsulates data stored within each `TableEntry` tracked by this object.
 */
internal interface Table<K, V> {

// CREATE functions

    /**
     * Adds the given [entry] to this table, returning the key identifier for the created entry.
     */
    fun create(entry: V): K

    /**
     * Creates all [entries] on this table.
     */
    fun createAll(entries: Iterable<V>) = entries.forEach { create(it) }

// GET functions

    /**
     * Returns the value in this table associated with the given [key].
     */
    operator fun get(key: K): V

    /**
     * Returns all values associated with this table.
     */
    fun getAll(): Iterable<V>

// UPDATE functions

    /**
     * Updates the given [entry] on this table.
     */
    fun update(entry: V)

    /**
     * Updates all the given [entries] on this table.
     */
    fun updateAll(entries: Iterable<V>) = entries.forEach{ update(it) }

// DELETE functions

    /**
     * Deletes the value in this table associated with the given [key].
     */
    fun delete(key: K)

// Utility functions

    /**
     * Returns the number of values contained within this table.
     */
    fun size(): Int

}
