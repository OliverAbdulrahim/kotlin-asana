package org.conservationco.asana.serialization.stubs

import com.asana.models.Attachment
import org.conservationco.asana.serialization.AsanaSerializable
import org.conservationco.asana.serialization.customfield.AsanaCustomField

class Person(
    var id: String = "",
    var name: String = "",
    var documents: Collection<Attachment> = emptyList(),
    @AsanaCustomField("Favorite dessert") var favoriteDessert: String = "",
    @AsanaCustomField("Favorite season") var favoriteSeason: String = "",
    @AsanaCustomField("Languages spoken") var languagesSpoken: Array<String> = emptyArray(),
) : AsanaSerializable<Person>
