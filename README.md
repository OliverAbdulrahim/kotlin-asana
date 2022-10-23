# Welcome to `kotlin-asana`!
This repository is home to a Kotlin wrapper for `java-asana`, which is aimed at making the client simple, declarative, 
and fun to use! 

While `kotlin-asana` does not exhaustively implement all endpoints of the Asana REST API, it covers common use cases (at
least those at our organization!). We've implemented useful features extending `java-asana`'s functionality, such as 
working with project templates, custom field operations, easy pagination handling, and more. Essentially, this library
handles the intricacies of making your API calls work properly, so you can focus on writing your business logic! 

Read on to learn more and find examples.

## Jump to a section
1. [Overview of features](#overview)
2. [Installation (with Maven)](#installation-with-maven)
    * [Add this project as a dependency](#add-this-project-as-a-dependency)
    * [Add JitPack as a repository](#add-jitpack-as-a-repository)
3. [Setup](#setup)
   * [Supplying your access token](#supplying-your-access-token)
4. [Usage examples](#usage-examples)
    * [Using the `asanaContext` entrypoint](#using-the-asanacontext-entrypoint)
    * [Supply a configuration (optional)](#supply-a-configuration-to-asanacontext-optional)
    * [Working with resources](#working-with-resources)

## Overview of features 
### This library implements / supports:
1. Declaratively working with custom fields, tasks, projects, and workspaces.
2. Automagically handles pagination, passing query parameters, and other intricate setup work for you!
3. Searching for tasks within a workspace or project, with support for filters.
4. Enforces best practices for handling your access token.
5. Using projects as data tables, with support for serializing and deserializing Tasks and their CustomFields into data objects.

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
For autowired setup of your access token, supply the `asana_access_token` environment variable wherever you use 
`kotlin-asana`.

```
java -jar <your app name>.jar asana_access_token=<your access token>
```

If you store your access token some other way, instantiate `AsanaConfig` object and [pass that into your calls to 
`asanaContext`](#supply-a-configuration-to-asanacontext-optional).
```
val config = AsanaConfig("your access token")
```

## Usage examples
### Using the `asanaContext` entrypoint
Call the `asanaContext` [top level function](https://kotlinlang.org/docs/functions.html#function-scope) to start working
with Asana from anywhere in your codebase! 
```
asanaContext { <interact with Asana here> }
```
This client takes on the default configuration, autowired from configuration (like an access token) you pass in via 
environment variables. [You can always provide your own](#supply-a-configuration-to-asanacontext-optional); this is 
useful if you need to work in different contexts, for example, with multiple access tokens. 

When inside the scope of `asanaContext`, `this` refers to an `AsanaClient`. The function actually returns an 
`AsanaClient` object, so you can optionally store this for later use. 

### Supply a configuration to `asanaContext` (optional)
To provide your own configuration, use the `AsanaConfig` class to provide your own configuration and pass it into the 
`asanaContext` function. 
```
val config: AsanaConfig = ...

asanaContext(config) { ... }
```
### Working with resources
This library makes use of Kotlin [extension functions](https://kotlinlang.org/docs/extensions.html#extension-functions),
allowing you to cleanly and declaratively work with Asana Resources within any `asanaContext`, without having to worry
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
