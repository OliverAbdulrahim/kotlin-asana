package org.conservationco.asana

inline fun <R> asanaContext(
    clientExtension: AsanaClientExtension = ClientDefaults.CLIENT_EXT,
    block: AsanaClientExtension.() -> R
): R = clientExtension.block()

inline fun <R> asanaContext(
    config: AsanaConfig,
    block: AsanaClientExtension.() -> R
): R = asanaContext(AsanaClientExtension(config), block)
