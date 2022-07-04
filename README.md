# 🧸 kcctl – Your Cuddly CLI for Apache Kafka Connect

_It's Casey. Casey Cuddle._

This project is a command-line client for [Kafka Connect](https://kafka.apache.org/documentation/#connect).
Relying on the idioms and semantics of _kubectl_,
it allows you to register and examine connectors, delete them, restart them, etc.
You can see what _kcctl_ is about in this short video:

[![kcctl Intro](https://img.youtube.com/vi/F9bUsM1ZwKk/0.jpg)](https://www.youtube.com/watch?v=F9bUsM1ZwKk)

## Installation

The latest stable release of _kcctl_ (x86) for Linux, macOS, and Windows can be retrieved via [SDKMan](https://sdkman.io/sdks#kcctl):

```shell script
sdk install kcctl
```

You may also use [Homebrew](https://brew.sh/) to install _kcctl_ on Linux and macOs, by configuring our tap

```shell script
brew install kcctl/tap/kcctl
```

It is recommended to install the bash/zsh completion script _kcctl_completion_:

```shell script
wget https://raw.githubusercontent.com/kcctl/kcctl/main/kcctl_completion
. kcctl_completion
```

Alternatively, you can obtain early access binaries from [here](https://github.com/kcctl/kcctl/releases).
This is a rolling release, new binaries are published upon each commit pushed to the kcctl repository.

Note: on macOS, you need to remove the quarantine flag after downloading, as the distribution currently is not signed:

```shell script
xattr -r -d com.apple.quarantine path /to/kcctl-1.0.0-SNAPSHOT-osx-x86_64/
```

## Usage

### Quickstart

Before you can start using _kcctl_ you need to create a configuration context.
A configuration context is a set of configuration parameters, grouped
by a name, describing one particular Kafka Connect environment.
All subsequent commands will be executed using the currently active context.

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

### Available Commands

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
  describe  Displays detailed information about the specified resources
  apply     Applies the given files or the stdin content for registering or
              updating connectors
  patch     Modifies the configuration of some connectors or a logger
  restart   Restarts some connectors or a task
  pause     Pauses connectors
  resume    Resumes connectors
  delete    Deletes specified connectors
  help      Displays help information about the specified command
```

### Authentication

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

## Development

This project uses [Quarkus](https://quarkus.io/), the Supersonic Subatomic Java Framework.

To build the project, make sure to the following things are installed:

* Java 17
* Alternatively, for creating native binaries, GraalVM 22.1.0 or newer
* When using GraalVM, the native image tool (install via `$JAVA_HOME/bin/gu install native-image`)
* Docker must for running the integration tests (via Testcontainers)

The following build commands are commonly used:

```shell script
# Build and run all the tests
./mvnw clean verify

# Build and skip integration tests
./mvnw clean verify -Dquarkus.test.profile.tags="basic"

# Format sources
./mvnw process-sources
```

### Running the Application in Dev Mode

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

### Packaging and Running the Application

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

### Creating a Native Executable

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

Edit the completion script _kcctl\_completion_, replace all the quotes around generated completion invocations with back ticks, making them actual invocations of _kcctl_:

```shell script
--- local CONNECTOR_NAME_pos_param_args="kcctl connector-name-completions" # 0-0 values
+++ local CONNECTOR_NAME_pos_param_args=`kcctl connector-name-completions` # 0-0 values
```

Currently, three kinds of completions exist: `connector-name-completions`, `task-name-completions`, and `logger-name-completions`.

### Related Quarkus Guides

- Picocli ([guide](https://quarkus.io/guides/picocli)): Develop command line applications with Picocli
- Quarkus native apps ([guide](https://quarkus.io/guides/maven-tooling.html)): Develop native applications with Quarkus and GraalVM

## License

This code base is available under the Apache License, version 2.
