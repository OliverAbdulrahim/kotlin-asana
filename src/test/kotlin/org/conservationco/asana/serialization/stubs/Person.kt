package org.conservationco.asana.serialization.stubs

import org.conservationco.asana.serialization.AsanaSerializable
import org.conservationco.asana.serialization.customfield.AsanaCustomField

class Person(
    var id: String = "",
    var name: String = "",
    @AsanaCustomField("Favorite dessert") var favoriteDessert: String = "",
    @AsanaCustomField("Favorite season") var favoriteSeason: String = "",
    @AsanaCustomField("Languages spoken") var languagesSpoken: Array<String> = emptyArray(),
) : AsanaSerializable<Person>
