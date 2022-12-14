# `kotlin-asana` serialization handbook
This document will guide you through how to serialize data to and from Asana using this library! You can turn any `Task`
into your own custom data object, and back to a `Task` again. We prefer convention to configuration, so we've set up the
default behavior to do all the work of object construction for you. That said, this library is _extensible_! Should you
need to, you can write your own serializer, which we discuss below.

## Jump to a section
1. [Why this type of serialization useful?](#why-this-type-of-serialization-useful)
   * [Type safety](#type-safety-is-the-biggest-motivation)
   * [Loose coupling](#loose-coupling-is-another-motivation)
   * [Encapsulation](#encapsulation-of-data-as-well)
2. [Example: working with a project with friends from your travels!](#usage-example-project-with-friends-from-your-travels)
   1. [Working with Asana: the naive way](#working-with-asana-data-the-naive-way)
      * [Code example (Kotlin)](#code-example-kotlin)
      * [Problems with this approach](#problems-with-this-approach)
   2. [Working with Asana: an idiomatic approach](#working-with-asana-data-an-idiomatic-approach-with-kotlin-asana)
      * [Updated code example (Kotlin)](#updated-code-example-kotlin)
      * [Advantages of this updated approach](#advantages-of-this-updated-approach)
      * [It gets better: turn your objects back into tasks](#it-gets-better-turn-your-objects-back-into-tasks)

## Why this type of serialization useful?
### Type safety is the biggest motivation.
Asana resources are designed for cross-platform use and are served as JSON. You'll get the direct unmarshalling of that
JSON when using client libraries like `java-asana`. That mapping of data is relatively fast, but not idiomatic to
statically typed languages like Java and Kotlin. Critically, since your data isn't always mapped to its true type, your
code ends up prone to errors and mistakes.

This library brings you back to the land of type safety, mapping your data to a model that you define, name, and
provide types for.

### Loose coupling is another motivation.
This library allows your business logic to happen on objects that are part of your data model, not on Asana resources.
That makes it easy for future you to switch to a tool supplementing or replacing Asana in your stack. You wouldn't need
to rewrite any business logic and your data ends up easy to migrate.

### Encapsulation of data as well.
This library empowers you to model and bundle your data together in a way that works best for your application.

## Usage example: project with friends from your travels!
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

### Working with Asana data: the naive way
Say you wanted to programmatically work with your friend data. Maybe you want to collect all the desserts that you've
enjoyed with your friends. Let's do that using `java-asana`:

#### Code example (Kotlin)
```kotlin
// Imports and client setup omitted
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

### Problems with this approach
We wrote a lot of not-so-pretty boilerplate to get to what we needed, which in this simple example was just a name and a
single custom field.
1. We had to make sure we got the right query options
   * We'd have to remember to set `name` and `custom_fields` each time we make a request
2. Working with `CustomField` objects is clumsy and inefficient
   * We have to iterate over a collection and compare Strings each time we need a custom field's value
3. Our code is not idiomatic and the data we care about is not well encapsulated!
   * We've had to hard code a field's name ("Favorite dessert") within our business logic
   * We don't have checks over what type of data that custom field actually represents (think dynamic typing without any
     of the advantages of a dynamic type system)

We could write some helper functions to address with #1 and #2. Perhaps one for setting queries on a given request.
Maybe another to find a custom field by name from a collection.

Problem #3 is the real challenge! It compounds quickly, too. What if we wanted to extract two, three, or even more
custom fields from each task? **What if we wanted to extract each custom field into a data object?** This implementation
breaks down as soon as we demand anything more.

Let's see how we can do better with `kotlin-asana`.

### Working with Asana data: an idiomatic approach with `kotlin-asana`
Let's start by taking a bottom-up, idiomatic approach. Why don't we model each friend into a simple class, `Person`?
Each property in `Person` will relate to parts of an Asana task. We'll let `kotlin-asana` do all the hard work of
mapping the data for us

Before we dive into details, here's what we'll do:
1. Define a `Person` class to encapsulate our friend data
2. Implement the `AsanaSerializable<T>` interface
3. Map each of the properties that are represented by custom fields with the `@AsanaCustomField` annotation
4. Refactor our code from the previous section

#### Updated code example (Kotlin)
```kotlin
class Person : AsanaSerializable<Person> {
   lateinit var id: String = ""
   lateinit var name: String = ""
   @AsanaCustomField("Favorite dessert") lateinit var favoriteDessert: String
   @AsanaCustomField("Favorite season")  lateinit var favoriteSeason: String
   @AsanaCustomField("Languages spoken") lateinit var languagesSpoken: Array<String>
}

// elsewhere, in a land far away
val projectGid = "12345"
val people: List<Person> = asanaContext {
   project(projectGid) {
      convertTasksToListOf(Person::class) { source: Task, destination: Person ->
         destination.name = source.name
         destination.id = source.gid
      }
   }
}
people.forEach { person -> println("${person.name} loves eating ${person.favoriteDessert}!")}
```
#### Advantages of this updated approach
`kotlin-asana` does all the messy work for you:
1. Autowires you a client (from environment variables you provide)
   * You could also specify your own client!
2. Sets up and executes your requests for you
   * Even handles pagination; whether you have 1 task or 101+ tasks, you get them in the same way
3. Maps your task's custom fields into your own object, in this case, `Person`
   * Your data is encapsulated properly and is easy to access
   * Types are preserved in object properties
   * Need to map other parts of a task, like name, id, and attachments? No problem, just tell `kotlin-asana` how
4. Provides a declarative, readable, Kotlin-idiomatic way to work with Asana resources

#### It gets better: turn your objects back into tasks 
As easily as you converted `Person` into `Task`, you can also turn each `Person` object back into a `Task`! In a 
real-world example, you'd likely do this within the same `asanaContext`, as shown below.

```kotlin
asanaContext {
    project(projectGid) {
        val people: List<Person> = convertTasksToListOf(Person::class) 
        // ... call your business logic on each Person in people ... 
        people.convertToTaskList(this).forEach { task -> task.update() }
    }
}
```
