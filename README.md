# Welcome to `kotlin-asana`!
This repository is home to a Kotlin wrapper for `java-asana`, which is aimed at making the client simple, declarative, 
and fun to use! While `kotlin-asana` is not meant to exhaustively implement all endpoints provided by the Asana REST 
API, it covers common use cases and adds useful features that are not implemented in `java-asana`.

## Jump to a section
1. [Overview](#overview)
2. [Examples](#examples)
    * [Using the `asanaContext` entrypoint](#using-the-asanacontext-entrypoint)
    * [Supply a configuration (optional)](#supply-a-configuration-to-asanacontext)
    * [Working with resources](#working-with-resources)

## Overview 
### This library implements / supports:
1. Declaratively working with custom fields, tasks, projects, and workspaces.
2. Searching for tasks within a workspace or project, with support for filters.
3. Using projects as data tables, with support for serializing and deserializing Tasks into data objects.
4. Enforcing best practices for handling your access token.

## Examples

### Using the `asanaContext` entrypoint
Call the `asanaContext` extension function to start working with Asana from anywhere in your code base! 
When inside the scope of this function, `this` refers to an `AsanaClient`. This client takes on the default 
configuration, but you can always provide your own; this is useful if you need to work in different contexts, for 
example, with multiple access tokens.
```
asanaContext {  < this: AsanaClient > }
```
### Supply a configuration to `asanaContext`
Use the `AsanaConfig` class to provide your own configuration and pass it into the `asanaContext` function. 
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
   task.createAttachment(...)
   task.delete()
   
   // Projects
   val project = selectProject(projectGid = "56789")
   val tasks: List<Task> = project.getTasks(includeAttachments = true, expanded = true)
   
   // Workspaces
   val workspace = selectWorkspace(workspaceGid = "09876")
   val tasks: List<Task> = workspace.search(textQuery = "ice cream sundae", "project1", "project2", ...)  
}
```

More examples coming soon!