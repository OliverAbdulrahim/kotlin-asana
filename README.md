# Welcome to `kotlin-asana`!
This repository is home to a library for Asana written in Kotlin, which implements task ⇔️ object serialization, a DSL 
for working with resources, auto pagination handling, and more! 

`kotlin-asana` is aimed at extending the client's functionality with simple, declarative, and fun to use calls! 
Essentially, this library saves you from spending lots of time crafting API calls, so you can focus on your business 
logic. Read on to learn more and find examples. 

## Jump to a section
1. [Overview of features](#overview-of-features)
2. [Quick examples](#quick-examples)
3. [Installation (with Maven)](#installation-with-maven)
   1. [Add this project as a dependency](#add-this-project-as-a-dependency)
   2. [Add JitPack as a repository](#add-jitpack-as-a-repository)
4. [Quick setup](#quick-setup)
   * [Supply your access token via environment variables](#supply-your-access-token-via-environment-variables)
   * [Alternative authentication methods](#alternative-authentication-methods)
5. Configuration
   * [Default behavior](src/main/kotlin/org/conservationco/asana/README.md#configuration-default-behavior)
   * [Providing your own configuration](src/main/kotlin/org/conservationco/asana/README.md#configuration-provide-your-own)
6. Usage examples
    * Getting started with this library
      * [Using the `asanaContext` entrypoint](src/main/kotlin/org/conservationco/asana/README.md#using-the-asanacontext-entrypoint-function)
      * [Working with resources](#working-with-resources)
    * Serializing user-defined objects into tasks and back
      * [Why, motivations, and use cases](src/main/kotlin/org/conservationco/asana/serialization/README.md#why-this-type-of-serialization-useful)
      * [Working with Asana data: an idiomatic approach with `kotlin-asana`](src/main/kotlin/org/conservationco/asana/serialization/README.md#working-with-asana-data-an-idiomatic-approach-with-kotlin-asana)

## Overview of features 
### This library implements / supports:
1. Support for serializing and deserializing `Tasks` and their `CustomField`s into data objects.
2. Declaratively working with custom fields, tasks, projects, and workspaces.
3. Automagically handles pagination, passing query parameters, and other intricate setup work for you!
4. Searching for tasks within a workspace or project, with support for filters.
5. Enforces best practices for handling your access token.

## Quick examples
### Working with resources
This library makes use of Kotlin [function literals with receiver](https://kotlinlang.org/docs/lambdas.html#function-literals-with-receiver)
and [extension functions](https://kotlinlang.org/docs/extensions.html#extension-functions); these allow you to cleanly 
and declaratively work with Asana resources like tasks, projects, and workspaces within any `asanaContext` (the entry
point for this library).

```kotlin
asanaContext {
    // Tasks
    val person: Person = task("123") { this.convertTo(Person::class) }

    // Projects
    val taskAgain: Task = project("456") { person.convertToTask(this).update() }

    // Workspaces
    val search: List<Task> = workspace("789") { search("ice cream sundae", "456") }
}
```
Learn more about `asanaContext` [at this link](src/main/kotlin/org/conservationco/asana/README.md). More information on
this library's serialization [at this link](src/main/kotlin/org/conservationco/asana/serialization/README.md).

## Installation (with Maven) 
Deployment of this project to Maven central coming soon. For now, use JitPack.
### Add this project as a dependency
Include this git repository into your project's `pom.xml` with the following dependency:

```xml
<dependency>
   <groupId>com.github.OliverAbdulrahim</groupId>
   <artifactId>kotlin-asana</artifactId>
</dependency>
```
### Add JitPack as a repository
You'll also want to make sure that you have the [JitPack](https://jitpack.io/) repository:
```xml
<repository>
   <id>jitpack.io</id>
   <url>https://jitpack.io</url>
</repository>
```

## Quick setup
### Supply your access token via environment variables
_Recommended:_ for easy, autowired setup, supply the `asana_access_token` environment variable wherever you use 
`kotlin-asana`.

```
java -jar <your app name>.jar asana_access_token=<your access token>
```

### Alternative authentication methods
If you prefer to authenticate another way, or if you already have a `java-asana` `Client` object, follow these steps to
[provide `kotlin-asana` your own configuration (links to another document on this repository)](src/main/kotlin/org/conservationco/asana/README.md#providing-your-own-configuration). 
