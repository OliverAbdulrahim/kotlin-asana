## Configuration: default behavior
If you pass an access token as an environment variable, `kotlin-asana` will autowire a `java-asana` `Client` object for
you, without any additional configuration needed. This `Client` is then reused for any subsequent calls you make.

#### Example: passing in your access token as an environment variable
```shell
java -jar <your app name>.jar asana_access_token=<your access token>
```

## Configuration: provide your own
Follow these steps to provide your own configuration.
1. Create an object of the `AsanaConfig` class.
2. Use the constructor to specify your desired options.
    * You can mix and match any of the following types of configuration:
        * A `java-asana` `Client`(optional)
        * Verbosity of logs (optional)
        * Fields to include when making GET requests (optional)
        * Expanded responses (optional - useful for debugging)
3. Once configured, instantiate a `AsanaClientExtension`
4. Store this object to use each time you call [the `asanaContext` function](#using-the-asanacontext-entrypoint-function).
```kotlin
val config = AsanaConfig(client = ..., verboseLogs = ..., expandedResponses = ..., fields = arrayOf("...", "..."))
val ext = AsanaClientExtension(config)

asanaContext(ext) { ... }
```

## Using the `asanaContext` entrypoint function
To quickly start working with asana from anywhere in your codebase, call the `asanaContext`
[top level function](https://kotlinlang.org/docs/functions.html#function-scope).
```kotlin
asanaContext { <interact with Asana here> }
```
When inside the scope of `asanaContext`, `this` refers to an `AsanaClientExtension`. The function returns any value `R`,
which allows you to more conveniently escape objects you declare or obtain within its scope — simply assign the return
value of `asanaContext` to the desired object.

Note that, to call `asanaContext` without specifying any parameters, you _must_ pass in your access token as an
environment variable. Under the hood, `kotlin-asana` autowires a `java-asana` `Client` object for you, which is then
reused for any subsequent calls you make.

If you cannot pass in environment variables, or if you need to authenticate another way,
[instantiate your own `Client`](#configuration-provide-your-own) and provide it each time you call `asanaContext`.
