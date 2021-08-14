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
  get       Displays information about connector plug-ins, connectors, and
              loggers
  describe  Displays detailed information about the specified resource
  apply     Applies the given file for registering or updating a connector
  patch     Modifies the configuration of a connector or logger
  restart   Restarts a connector or task
  pause     Pauses a connector
  resume    Resumes a connector
  delete    Deletes the specified connector
  help      Displays help information about the specified command
```

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

You can then execute your native executable with: `./target/code-with-quarkus-1.0.0-SNAPSHOT-runner`

As above, either define an alias _kcctl_ or rename the resulting executable accordingly.

### Updating the Completion Script

Grep for usages of `DummyCompletions` and uncomment them
(they are used as placeholders in the generated completion script).

Build the application in JVM mode.

Recreate the completion script:

```shell script
java -cp "target/quarkus-app/app/*:target/quarkus-app/lib/main/*:target/quarkus-app/quarkus-run.jar" \
  picocli.AutoComplete -n kcctl --force dev.morling.kccli.command.KcCtlCommand
```

Edit the completion scrpt _kcctl_completion_, replace all the dummy completion placeholders with invocations of one of the (hidden) completion candidate commands, e.g. like so:

```shell script
--- local CONNECTOR_NAME_pos_param_args="connector-1 connector-2 connector-3" # 0-0 values
+++ local CONNECTOR_NAME_pos_param_args=`kcctl connector-name-completions` # 0-0 values
```

### Related Quarkus Guides

- Picocli ([guide](https://quarkus.io/guides/picocli)): Develop command line applications with Picocli
- Quarkus native apps ([guide](https://quarkus.io/guides/maven-tooling.html)): Develop native applications with Quarkus and GraalVM

## License

This code base is available ander the Apache License, version 2.
