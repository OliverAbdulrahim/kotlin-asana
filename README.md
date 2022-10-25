# Welcome to `kotlin-asana`!
This repository is home to a Kotlin wrapper for `java-asana`, which is aimed at extending the client's functionality
designed around simple, declarative, and fun to use calls! 

`kotlin-asana` makes common use cases a breeze and also implements useful features extending `java-asana`'s 
functionality. Use this library to easily convert your own data objects into Asana `Tasks` and back. Easily work with 
custom fields, handle pagination automatically, work with project templates, and much more. 

Essentially, this library allows you to focus on writing your business logic instead of intricate API calls! Read on to
learn more and find examples. 

## Jump to a section
1. [Overview of features](#overview-of-features)
2. [Installation (with Maven)](#installation-with-maven)
   1. [Add this project as a dependency](#add-this-project-as-a-dependency)
   2. [Add JitPack as a repository](#add-jitpack-as-a-repository)
3. [Setup](#setup)
   * [Supply your access token via environment variables](#supply-your-access-token-via-environment-variables)
   * [Alternative authentication methods](#alternative-authentication-methods)
4. [Configuration](#configuration)
   * [Default behavior](#default-behavior)
   * [Providing your own configuration](#providing-your-own-configuration)
5. [Usage examples](#usage-examples)
    * [Using the `asanaContext` entrypoint](#using-the-asanacontext-entrypoint-function)
    * [Working with resources](#working-with-resources)

## Overview of features 
### This library implements / supports:
1. Support for serializing and deserializing `Tasks` and their `CustomField`s into data objects.
2. Automagically handles pagination, passing query parameters, and other intricate setup work for you!
3. Declaratively working with custom fields, tasks, projects, and workspaces.
4. Searching for tasks within a workspace or project, with support for filters.
5. Enforces best practices for handling your access token.

## Installation (with Maven) 
### Add this project as a dependency
Include this git repository into your project's `pom.xml` with the following dependency:

```
<dependency>
   <groupId>com.github.OliverAbdulrahim</groupId>
   <artifactId>kotlin-asana</artifactId>
</dependency>
```

### Add JitPack as a repository
You'll also want to make sure that you have the [JitPack](https://jitpack.io/) repository:
```
<repository>
   <id>jitpack.io</id>
   <url>https://jitpack.io</url>
</repository>
```

## Setup
### Supply your access token via environment variables
_Recommended:_ for the easy, autowired setup, supply the `asana_access_token` environment variable wherever you use 
`kotlin-asana`.

```
java -jar <your app name>.jar asana_access_token=<your access token>
```

### Alternative authentication methods
If you prefer to authenticate another way, or if you already have a `java-asana` `Client` object, follow these steps
to [provide `kotlin-asana` your own configuration](#providing-your-own-configuration). 

## Configuration
### Default behavior
If you [pass an access token as an environment variable](#supply-your-access-token-via-environment-variables), 
`kotlin-asana` will autowire a `java-asana` `Client` object for you, without any additional configuration needed. 

### Providing your own configuration
Follow these steps to provide your own configuration:
1. Create an object of the `AsanaConfig` class.
2. Use the constructor to specify your desired options. 
   * You can mix and match any of the following types of
      configuration:
      * A `java-asana` `Client`(optional)
      * A `CustomFieldContext` (required only if you're serializing Tasks into custom data objects and back)
      * Verbosity of logs (optional)
3. Once configured, instantiate a `AsanaClientExtension`
4. Store this object to use each time you call 
[the `asanaContext` function](#using-the-asanacontext-entrypoint-function).
```
val config = AsanaConfig(client = ..., context = ..., verboseLogs = ...)
val ext = AsanaClientExtension(config)

asanaContext(ext) { ... }
```

## Usage examples
### Using the `asanaContext` entrypoint function
To quickly start working with asana from anywhere in your codebase, call the `asanaContext` 
[top level function](https://kotlinlang.org/docs/functions.html#function-scope).
```
asanaContext { <interact with Asana here> }
```
When inside the scope of `asanaContext`, `this` refers to an `AsanaClientExtension`. The function returns any value `R`,
which allows you to more conveniently escape objects you declare or obtain within its scope — simply assign the return 
value of `asanaContext` to the desired object.

Note that, to call `asanaContext` without specifying any parameters, you _must_ pass in your access token as an 
environment variable. Under the hood, `kotlin-asana` autowires a `java-asana` `Client` object for you, which is then
reused for any subsequent calls you make.

If you cannot pass in environment variables, or if you need to authenticate another way, 
[instantiate your own `Client`](#providing-your-own-configuration) and provide it each time you call 
`asanaContext`.

### Working with resources
This library makes use of Kotlin [extension functions](https://kotlinlang.org/docs/extensions.html#extension-functions),
allowing you to cleanly and declaratively work with Asana resources within any `asanaContext`, without having to worry
about the specific API implementation:

```
asanaContext {
   
   // Tasks
   val task = selectTask(taskGid = "12345")
   val customField = task.findCustomField("name")
   val attachment = task.createAttachment(Attachment())
   task.delete()
   
   // Projects
   val project = selectProject(projectGid = "56789")
   val tasks: List<Task> = project.getTasks(includeAttachments = true, expanded = true) // pagination is handled for you
   
   // Workspaces
   val workspace = selectWorkspace(workspaceGid = "09876")
   val tasks: List<Task> = workspace.search(textQuery = "ice cream sundae", "project1", "project2", ...)  
   
}
```

More examples coming soon!
