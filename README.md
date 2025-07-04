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

## 🔧 Configuration

You can customize the behavior of each task using the following parameters. These can be provided through command-line tools like Maven, environment variables, or config files (like `.env`). All parameters are **optional**, and sensible defaults are used where appropriate.

> **Important**: We strongly advise you to use a `.env` file which contains most of the parameters

> **Note**: Values passed through the command line, override `.env` values
### 🧱 General Options (Not included in .env)

| Parameter               | Type    | Default | Description                                                       |
| ----------------------- | ------- | ------- |-------------------------------------------------------------------|
| `includeOracleFixing`   | Boolean | `true`  | Whether to include automatic oracle fixing logic.                 |
| `outputDirectory`       | String  | `null`  | Directory where generated output will be saved.                   |
| `modelName`             | String  | `null`  | Name of the model to be used.                                     |
| `config`                | String  | `.env`  | Path to the environment config file.                              |

### 🤖 LLM Integration

| Parameter         | Type   | Description                                             |
| ----------------- | ------ | ------------------------------------------------------- |
| `gptApiKey`       | String | API key for OpenAI GPT model access.                    |
| `gptOrganization` | String | OpenAI organization ID (if needed).                     |
| `gptModel`        | String | Name of the GPT model to use (e.g., `gpt-4`, `gpt-4o`). |
| `deepseekApiKey`  | String | API key for DeepSeek model access.                      |
| `deepseekBaseUrl` | String | Base URL for DeepSeek API.                              |
| `ollamaChatUrl`   | String | Local Ollama chat server URL (if using Ollama).         |

### 🔁 Execution Controls

Fine-tune the number of iterations for various stages of the fixing and generation process:

| Parameter                          | Type | Description                                     |
| ---------------------------------- | ---- | ----------------------------------------------- |
| `testFramework`         | String  | Name of the test framework to use (e.g., JUnit5, TestNG).         |
| `lang`                  | String  | Programming language of the source code (e.g., `java`, `kotlin`). |
| `removeNonPassingTests` | Boolean | Whether to remove tests that do not pass during execution.        |
| `dirPrompts`            | String  | Directory containing prompt templates.                            |
| `dirOutput`             | String  | Directory where generated files will be stored.                   |
| `maxFixIterations`                 | Int  | Max number of fixing iterations.                |
| `maxGenerateIterations`            | Int  | Max number of test generation iterations.       |
| `maxFixUsingMoreContent`           | Int  | Max attempts to fix using additional content.   |
| `maxFixWrongInvocations`           | Int  | Max retries for fixing incorrect method calls.  |
| `maxRemoveTestsIterations`         | Int  | Max retries to remove non-working tests.        |
| `maxRepeatFailedIterations`        | Int  | Max retries for re-running failed tests.        |
| `maxRepeatFailedApiIterations`     | Int  | Max retries for failing API-based iterations.   |
| `maxFixOracleIterations`           | Int  | Max retries for fixing oracle-related issues.   |
| `maxFixOracleUsingModelIterations` | Int  | Max retries for fixing oracles using the model. |

### 📦 Miscellaneous

| Parameter         | Type   | Description                                                  |
| ----------------- | ------ | ------------------------------------------------------------ |
| `requiredImports` | String | Comma-separated list of required imports for generated code. |

---

### 💡 Usage Example (via Maven)

```bash
mvn exec:java -DincludeOracleFixing=false -DmodelName=my-model -DgptApiKey=your-key
```