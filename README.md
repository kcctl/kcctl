# 🧸 kcctl – Your Cuddly CLI for Apache Kafka Connect

This project is a command-line client for [Kafka Connect](https://kafka.apache.org/documentation/#connect).
Relying on the idioms and semantics of _kubectl_,
it allows you to register and examine connectors, delete them, restart them, etc.
You can see what _kcctl_ is about in this short video:

[![kcctl Intro](https://img.youtube.com/vi/F9bUsM1ZwKk/0.jpg)](https://www.youtube.com/watch?v=F9bUsM1ZwKk)

## Installation

You can obtain early access binaries of _kcctl_ (x86) for Linux, macOS, and Windows from [here](https://github.com/kcctl/kcctl/releases).
This is a rolling release, new binaries are published upon each commit pushed to the kcctl repository.

Note: on macOS, you need to remove the quarantine flag after downloading, as the distribution currently is not signed:

```shell script
xattr -r -d com.apple.quarantine path /to/kcctl-1.0.0-SNAPSHOT-osx-x86_64/
```

We're planning to publish _kcctl_ binaries via [SDKMAN!](https://sdkman.io/) soon, too.

It is recommended to install the bash/zsh completion script _kcctl_completion_:

```shell script
. kcctl_completion
```

## Quickstart

Before you can start using _kcctl_ you need to create a configuration context.
A configuration context is a set of configuration parameters, grouped
by a name.
To create a configuration context named `local`, with the Kafka Connect cluster URL set to
`http://localhost:8083`, issue the following command

```shell script
kcctl config set-context local --cluster http://localhost:8083
```

:exclamation: Note that certain commands will require additional parameters, like `bootstrap-servers` and
`offset-topic`.

Type `kcctl info` to display some information about the Kafka Connect cluster.
The command will use the currently active context, `local` in this case, to
resolve the cluster URL.

## Authentication

If your cluster enforces authentication, you may configure your username and password with the `username` and `password` parameters:

```shell script
kcctl config set-context local --cluster http://localhost:8083 --username myusername --password mypassword
```

:exclamation: Note that setting user name and password via CLI may store those credentials in your terminal history. To work around this, you may set the username and password directly in your `.kcctl` file:

```json
  "currentContext" : "local",
  "local" : {
    "cluster" : "http://localhost:8083",
    "username" : "myusername",
    "password" : "mypassword"
  }
```

Currently, only basic authentication is supported.

## Usage

Display the help to learn about using _kcctl_:

```shell script
kcctl help
Usage: kcctl [-hV] [COMMAND]
A command-line interface for Kafka Connect
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  info      Displays information about the Kafka Connect cluster
  config    Sets or retrieves the configuration of this client
  get       Displays information about connectorName plug-ins, connectors, and
              loggers
  describe  Displays detailed information about the specified resource
  apply     Applies the given file for registering or updating a connectorName
  patch     Modifies the configuration of a connectorName or logger
  restart   Restarts a connectorName or task
  pause     Pauses a connectorName
  resume    Resumes a connectorName
  delete    Deletes the specified connectorName
  help      Displays help information about the specified command
```

Start by running `kcctl config set-context <name> --cluster=<Kafka Connect URI)...` for setting up a configuration context which will be used by any subsequent commands.

## Development

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

To seed the command line arguments, pass the `-Dquarkus.args` option:

```shell script
./mvnw compile quarkus:dev -Dquarkus.args='patch get connectors'
```

In dev mode, remote debuggers can connect to the running application on port 5005.
In order to wait for a debugger to connect, pass the `-Dsuspend` option.

### Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.
You should define an alias _kcctl_:

```shell script
alias kcctl="java -jar target/quarkus-app/quarkus-run.jar"
```

### Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Pnative
```

You can then execute your native executable with: `./target/kcctl-1.0.0-SNAPSHOT-runner`

As above, either define an alias _kcctl_ or rename the resulting executable accordingly.

### Updating the Completion Script

Build the application in JVM mode.
Then recreate the completion script:

```shell script
java -cp "target/quarkus-app/app/*:target/quarkus-app/lib/main/*:target/quarkus-app/quarkus-run.jar" \
  picocli.AutoComplete -n kcctl --force org.kcctl.command.KcCtlCommand
```

Edit the completion script _kcctl_completion_, replace all the quotes around generated completion invocations with back ticks, making them actual invocations of _kcctl_::

```shell script
--- local CONNECTOR_NAME_pos_param_args="kcctl connector-name-completions" # 0-0 values
+++ local CONNECTOR_NAME_pos_param_args=`kcctl connectorName-name-completions` # 0-0 values
```

Currently, three kinds of completions exist: `connector-name-completions`, `task-name-completions`, and `logger-name-completions`.

### Related Quarkus Guides

- Picocli ([guide](https://quarkus.io/guides/picocli)): Develop command line applications with Picocli
- Quarkus native apps ([guide](https://quarkus.io/guides/maven-tooling.html)): Develop native applications with Quarkus and GraalVM

## License

This code base is available under the Apache License, version 2.
