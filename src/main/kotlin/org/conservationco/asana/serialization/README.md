# Serialization overview
This read me will guide you through how to serialize data to and from Asana using this library! You can turn any `Task`
into your own custom data object, and back to a `Task` again. We prefer convention to configuration, so we've set up the
default behavior to do all the work of object construction for you. That said, this library is _extensible_! Should you
need to, you can write your own serializer, which we discuss below.

## Jump to a section
1. [Why this type of serialization useful?](#why-this-type-of-serialization-useful)
   * [Type safety](#type-safety-is-the-biggest-motivation)
   * [Loose coupling](#loose-coupling-is-another-motivation)
2. [Example: people project](#usage-example-people-project)

### Why this type of serialization useful?
Great question!

#### Type safety is the biggest motivation. 
Asana resources are designed for cross-platform use and are served as JSON. You'll get the direct unmarshalling of that 
JSON when using client libraries like `java-asana`. That mapping of data is relatively fast, but not idiomatic to 
strongly typed languages like Java and Kotlin. Critically, your code ends up prone to errors and mistakes, as your data
isn't always mapped to its true type.

This library brings you back to the land of type safety, mapping your data to a model that you define, name, and 
provide types for. 

#### Loose coupling is another motivation.
This library allows your business logic to happen on objects that are part of your data model, not on Asana resources.
That makes it easy for future you to switch to a tool supplementing or replacing Asana in your stack. You wouldn't need
to rewrite any business logic and your data ends up easy to migrate.

### Usage example: project with friends from your travels!
Let's say you're working with an Asana project called "Friends from travels". Each task in this project represents 
someone you met touring the globe. Here's a screenshot:

![project.png](../../../../../resources/images/example-asana-people-project.png 'Screenshot of an Asana project with
tasks representing various people, with these columns: "Task name", "Favorite dessert", "Favorite season", and 
"Languages spoken"')

These are dear friends of yours, so you've tracked important information about them, such as:
* Their name (_Task name_)
* Sweets you enjoyed together (_Favorite dessert_)
* The time of year they like best (_Favorite season_)
* Languages you chatted with them in (_Languages spoken_)

### Collecting names and favorite desserts: the naive implementation
Say you wanted to programmatically work with your friend data. Maybe you want to collect all the desserts that you've
enjoyed with your friends. Let's do that using `java-asana`:

#### The Kotlin code
```
// Imports and client setup ommitted
val client = Client.accessToken("...")

// Construct the GET request on the project with my friends
val projectGid = "12345"
val request = client.tasks.getTasksForProject(projectGid, LocalDate.EPOCH.toString())
request.query["opt_fields"] = "name, custom_fields"
val tasks: List<Task> = request.execute()

// Map each Task to a pair of my friends names to the value of their dessert custom field 
val namesToDesserts = tasks.map { task ->
    task.name to task.customFields.find { customField -> customField.name == "Favorite dessert" }?.textValue
}

// Print them out all pretty!
namesToDesserts.forEach { nameDessertPair ->
    println("${nameDessertPair.first} loves eating ${nameDessertPair.second}!")
}
```
That will work... But would you commit this?

Even though we only needed to get a name and a custom field in this simple example, we wrote a lot of boilerplate to get
to what we needed!
1. ⛔️ We had to make sure we got the right query options 
   * We'd have to remember to set `name` and `custom_fields` each time we make a request 
2. ⛔️ Working with `CustomField` objects is clumsy and inefficient
   * We have to iterate over a collection and compare Strings each time we need a custom field's value
3. ⛔️ Our code is not idiomatic and doesn't encapsulate data we care about! 
   * We've had to hard code a field's name ("Favorite dessert")
   * We don't have checks over what type of data that custom field actually represents (think dynamic typing without any
of the advantages of a dynamic type system) 

We could write some helper functions to help with #1 and #2. Perhaps one to help with setting queries on a given 
request. Maybe another to find a custom field by name from a collection. 

Problem #3 is the real challenge! What if we wanted to extract two, three, or even more custom fields from each task? 
**What if we wanted to extract each custom field into a data object?** This implementation breaks down as soon as we 
demand more.

Let's see how we can do better with `kotlin-asana`.

### Collecting names and favorite desserts: an upgraded, idiomatic approach
Let's start by taking a bottom-up, idiomatic approach. Why don't we model each friend into a simple class, `Person`?
```
class Person(
    var id: String = "",
    var name: String = "",
    var favoriteDessert: String = "",
    var favoriteSeason: String  = "",
    var languagesSpoken: Array<String> = emptyArray(),
)
```
That looks good! Each property we defined is related to parts of an Asana task. 

Now... why don't we let `kotlin-asana` do all the hard work of mapping the data for us? Here's what we'll do:
1. Implement the `AsanaSerializable<T>` interface
2. Map each of the properties that are represented by custom fields with the `@AsanaCustomField` annotation

Here's what our updated class looks like:
```
class Person(
    override var id: String = "",
    override var name: String = "",
    @AsanaCustomField("Favorite dessert")
    var favoriteDessert: String = "",
    @AsanaCustomField("Favorite season") 
    var favoriteSeason: String = "",
    @AsanaCustomField("Languages spoken") 
    var languagesSpoken: Array<String> = emptyArray(),
) : AsanaSerializable<Person>
```

Before we dive into details, let's refactor our code from the previous section: 
#### Turn your tasks into objects
```
val projectGid = "12345"
val people: List<Person> = asanaContext {
   project(projectGid) {
      convertTasksToListOf(Person::class)
   }
}
people.forEach { person -> println("${person.name} loves eating ${person.favoriteDessert}!")}
```

That's all! Doesn't it read so much better – it's almost sentence-like.

#### Advantages of this
`kotlin-asana` does all the messy work for you:
1. Autowires you a client (from environment variables)
   * You could also specify your own!
2. Sets up and executes your requests for you
   * Even handles pagination; whether you have 1 task or 101+ tasks, you get them in the same way
3. Converts your `Task`s into your custom objects, in this case, `Person` 

#### Killer feature: turn your objects into tasks
As easily as you converted `Person` into `Task`, now turn each `Person` object back into a `Task`!

```
asanaContext {
   project(projectGid) {
      people.convertToTaskList(this)
            .forEach { task -> task.update() }
   }
}
```

More documentation coming soon!