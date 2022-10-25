# Welcome to `kotlin-asana`!
This repository is home to a Kotlin wrapper for `java-asana`, which is aimed at making the client simple, declarative, 
and fun to use! 

`kotlin-asana` makes common use cases a breeze and also implements useful features extending `java-asana`'s 
functionality. Use this library to easily convert your own data objects into Asana Tasks and back. Easily work with 
custom fields, handle pagination automatically, work with project templates, and much more. 

Essentially, this library allows you to focus on writing your business logic instead of intricate API calls. Read on to
learn more and find examples. 

## Jump to a section
1. [Overview of features](#overview-of-features)
2. [Installation (with Maven)](#installation-with-maven)
   1. [Add this project as a dependency](#add-this-project-as-a-dependency)
   2. [Add JitPack as a repository](#add-jitpack-as-a-repository)
3. [Setup](#setup)
   * [Supplying your access token](#supplying-your-access-token)
4. [Usage examples](#usage-examples)
    * [Using the `asanaContext` entrypoint](#using-the-asanacontext-entrypoint)
    * [Supply a configuration (optional)](#supply-a-configuration-to-asanacontext)
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
### Supplying your access token
_Recommended method:_ for autowired setup of your access token, supply the `asana_access_token` environment variable 
wherever you use `kotlin-asana`.

```
java -jar <your app name>.jar asana_access_token=<your access token>
```

If you store your access token some other way, instantiate an `AsanaConfig` object and [pass that into your calls to 
`asanaContext`](#supply-a-configuration-to-asanacontext).
```
val config = AsanaConfig("your access token")
```

## Usage examples
### Using the `asanaContext` entrypoint
To quickly start working with asana from anywhere in your codebase, call the `asanaContext` 
[top level function](https://kotlinlang.org/docs/functions.html#function-scope).
```
asanaContext { <interact with Asana here> }
```
When inside the scope of `asanaContext`, `this` refers to an `AsanaClientExtension`. The function returns any value `R`,
which allows you to conveniently escape objects you declare or obtain within its scope — simply assign the return value 
of `asanaContext` to the desired object.

Note that, under the hood, each time you call `asanaContext` without specifying any parameters, `kotlin-asana` creates a
new `Client`. This `Client` takes on the default configuration, autowired from values (such as an access token) you've
passed in via environment variables. In other words, you must pass in your access token as an environment variable to 
use `asanaContext` without parameters.

If you plan on making calls to Asana in more than one context, and/or if you need to authenticate in a way other than
access tokens, [instantiate your own Asana client](#supply-a-configuration-to-asanacontext) and provide it each time you
call `asanaContext`.

### Supply a configuration to `asanaContext`
To provide your own configuration, use the `AsanaConfig` class to specify options such as a `java-asana` Client, a 
custom field context, and more. You can mix and match each type of configuration. Each is optional. Once configured, 
instantiate a `AsanaClientExtension`; store this object to use for each time you call the `asanaContext` function. 
```
val config = AsanaConfig(client = ..., context = ..., verboseLogs = ...)
val ext = AsanaClientExtension(config)

asanaContext(ext) { ... }
```
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
