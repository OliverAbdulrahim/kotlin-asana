package org.conservationco.asana.extensions.portfolios

import com.asana.models.Portfolio
import com.asana.models.Project
import org.conservationco.asana.AsanaConfig
import org.conservationco.asana.asanaContext
import org.conservationco.asana.extensions.collectPaginations

/**
 * Container class for requests related to [Portfolio] objects.
 */
class Portfolios(
    private val config: AsanaConfig,
) {

    private val client = config.client

    fun getItemsPaginated(portfolio: Portfolio): List<Project> {
        val request = client.portfolios.getItemsForPortfolio(portfolio.gid)
        return collectPaginations(request)
    }

}
