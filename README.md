# YATE - Official Java/Kotlin plugin


> Yet Another TEst generator

**YATE** is a research project for generating unit tests using LLMs. Its
approach is to incrementally provide more meaningful content when repairing the LLM's response 

### Table of contents

In this README, we highlight the following elements:

- [Installation](#installation)
  - [Build from source](#build-from-source)
  - [Configuration](#configuration)
  - [Execution guide: Available commands](#execution-guide-available-commands)


## Installation

### Build from source

1. Clone and install repository [michaelkonstantinou/yate-java](https://github.com/michaelkonstantinou/yate-java)
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
mvn yate:generate -DincludeOracleFixing=false -DmodelName=my-model -DgptApiKey=your-key
```

## Execution guide: Available Commands

### Test generation on class

**Generating tests for the whole class**
```bash
mvn yate:generate -DclassPath=full/path/to/cut.java
```

**Generating tests for a specific method of the class**
```bash
mvn yate:generate -DclassPath=full/path/to/cut.java -DmethodName=foo
```

**Exclusive to action parameters**

| Parameter    | Required | Default | Type   | Description                                                                            |
|--------------|----------|---------|--------|----------------------------------------------------------------------------------------|
| `classPath`  | Yes      | -       | String | The absolute path of class under test                                                  |
| `type`       | No       | CLASS   | String | The level of generation. Can be CLASS, METHOD, METHOD_RESTRICT, CONSTRUCTORS or HYBRID |
| `methodName` | No       | -       | String | The name of the method under test                                                      |

### Test generation via CSV file

The following action is extremely useful for mass-test generation, to target multiple classes of the same project.

For each row, the action will generate tests for the class and update the file with useful statistics.
```bash
mvn yate:generateUsingDataset -Dfile=full/path/to/file.csv
```

**Exclusive to action parameters**

| Parameter       | Required | Default     | Type   | Description                                                                                                                        |
|-----------------|----------|-------------|--------|------------------------------------------------------------------------------------------------------------------------------------|
| `file`          | Yes      | -           | String | The absolute path of the csv file                                                                                                  |
| `ablationSetup` | No       | NO_ABLATION | String | Whether the ablation runner should be used instead of the normal one. Useful to conduct experiments with the removal of components |

Sure! Here's a **bullet list** describing the expected structure of the input CSV file. Each row in your CSV should contain the following fields in this order:

---

#### 📄 CSV File Structure

Each row should include the following fields. Some of them are optional though and others are updated during execution:

* **`repositoryPath`** *(String)*: Path to the source code repository.
* **`classPath`** *(String)*: Path to the class under test.
* **`testLevel`** *(Enum: `CLASS`, `METHOD`, etc.)*: The type/level of testing to be performed.
* **`requests`** *(Object or counter)*: Number of LLM or tool requests made during processing. Updated during execution (can be initialized with 0 or null).
* **`generationTime`** *(Long / Milliseconds)*: Time taken to generate tests (in ms). Updated during execution (can be initialized with 0 or null).
* **`isExecuted`** *(Boolean)*: Indicates whether test execution has occurred successfully (`true` or `false`) Updated during execution (can be initialized with 0 or null).
* **`errors`** *(String, nullable)*: Any errors encountered during generation or execution (can be empty). Updated during execution (can be initialized with 0 or null).
* **`outputDir`** *(String, nullable)*: Directory where output (e.g., generated tests) is stored.
* **`modelName`** *(String, nullable)*: Name of the model used for test generation (e.g., `gpt-4`, `deepseek-coder`).
* **`generatedTests`** *(Int)*: Number of test cases successfully generated (beta version).

---