package org.conservationco.asana

data class AsanaConfig(
    val accessToken: String = System.getenv("asana_access_token"),
    val workspaceId: String = System.getenv("workspace_id"),
)
