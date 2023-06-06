package org.conservationco.asana.extensions.webhooks

import com.asana.models.Webhook
import com.google.gson.JsonElement
import org.conservationco.asana.AsanaConfig

class Webhooks(
    private val config: AsanaConfig,
) {

    private val client = config.client

    fun createWebhook(resourceGid: String, targetUrl: String): Webhook =
        client.webhooks
            .createWebhook()
            .data("resource", resourceGid)
            .data("target", targetUrl)
            .execute()

    fun deleteWebhook(webhookGid: String): JsonElement {
        return client.webhooks
            .deleteWebhook(webhookGid)
            .execute()
    }

    fun getWebhook(webhookGid: String): JsonElement {

        return client.webhooks
            .deleteWebhook(webhookGid)
            .execute()
    }

    fun getWebhooks(resourceGid: String, workspaceGid: String): List<Webhook> {
        return client.webhooks
            .getWebhooks(resourceGid, workspaceGid)
            .execute()
    }

    fun deleteWebhooks(resourceGid: String, workspaceGid: String) =
        getWebhooks(resourceGid, workspaceGid)
            .forEach { deleteWebhook(it.gid) }

}
