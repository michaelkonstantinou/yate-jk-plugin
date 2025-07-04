# YATE - Official Java/Kotlin plugin


> Yet Another TEst generator

**YATE** is a research project for generating unit tests using LLMs. Its
approach is to incrementally provide more meaningful content when repairing the LLM's response 

### Table of contents

In this README, we highlight the following elements:

- [Installation](#installation)
  - [Build from source](#build-from-source)
  - [Configuration](#configuration)


## Installation

### Build from source

1. Clone and install repository "yate-java"
2. Clone this repository and run `mvn clean install`
3. In the project you want to use YATE, add the following plugin in the `pom.xml` file

```xml
<!-- YATE plugin -->
<plugin>
    <groupId>com.mkonst</groupId>
    <artifactId>yate-jk-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <config>Path/to/.env</config>
    </configuration>
</plugin>
```

### Configuration

