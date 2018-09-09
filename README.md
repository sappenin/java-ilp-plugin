# Java Interledger Plugin Interface [![join the chat on gitter][gitter-image]][gitter-url] [![circle-ci][circle-image]][circle-url] [![codecov][codecov-image]][codecov-url]

[gitter-image]: https://badges.gitter.im/sappenin/java.svg
[gitter-url]: https://gitter.im/interledger/java
[circle-image]: https://circleci.com/gh/sappenin/java-ilp-plugin.svg?style=shield
[circle-url]: https://circleci.com/gh/sappenin/java-ilp-plugin
[codecov-image]: https://codecov.io/gh/sappenin/java-ilp-plugin/branch/master/graph/badge.svg
[codecov-url]: https://codecov.io/gh/sappenin/java-ilp-plugin

Java implementation of the [Plugin Interface 2](https://github.com/interledger/rfcs/blob/master/0024-ledger-plugin-interface-2/0024-ledger-plugin-interface-2.md), typically used by ILPv4 Connectors.

* v4.0.0-SNAPSHOT Initial commit of interfaces and abstract classes for ILPv4 Plugins.
 
## Usage

### Requirements
This project uses Maven to manage dependencies and other aspects of the build. 
To install Maven, follow the instructions at [https://maven.apache.org/install.html](https://maven.apache.org/install.html).

### Get the code

``` sh
git clone https://github.com/sappenin/java-ilp-plugin
cd java-ilp-plugin
```

### Build the Project
To build the project, execute the following command:

```bash
$ mvn clean install
```

#### Checkstyle
The project uses checkstyle to keep code style consistent. All Checkstyle
checks are run by default during the build, but if you would like to run
checkstyle checks, use the following command:


```bash
$ mvn checkstyle:checkstyle
```

### Step 3: Extend
This project is meant to be extended with your own implementation. There is one concrete
implementation of a Plugin in this project, called `SimulatedChildPlugin`, which is a demonstration
implementation that simulates a connection to a fake remote Node where the runtime operating the
plugin is a _child_ of the remote node (see [https://github.com/interledger/rfcs](https://github.com/interledger/rfcs)
for more details about Interledger relationships).

## Contributors
Any contribution is very much appreciated! 

[![gitter][gitter-image]][gitter-url]

## TODO
See the issues here: [https://github.com/sappenin/java-ilp-plugin/issues](https://github.com/sappenin/java-ilp-plugin/issues).

## License
This code is released under the Apache 2.0 License. Please see [LICENSE](LICENSE) for the full text.
