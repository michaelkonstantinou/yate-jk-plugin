package com.mkonst

import com.mkonst.config.ConfigYate
import com.mkonst.exceptions.InvalidInputException
import com.mkonst.helpers.YateConsole
import com.mkonst.runners.YateAbstractRunner
import com.mkonst.runners.YateJavaRunner
import com.mkonst.services.PromptService
import com.mkonst.types.ProgramLangType
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Parameter
import java.nio.file.Paths

abstract class AbstractYateMojo: AbstractMojo() {

    // Common task variables
    @Parameter(property = "repositoryPath", required = false, defaultValue = "")
    protected var repositoryPath: String = ""

    @Parameter(property = "includeOracleFixing", required = false)
    protected var includeOracleFixing: Boolean = true

    @Parameter(property = "outputDirectory", required = false)
    protected var outputDirectory: String? = null

    @Parameter(property = "modelName", required = false)
    protected var modelName: String? = null

    // Env variables
    @Parameter(property = "config", required = false, defaultValue = ".env")
    protected lateinit var config: String

    @Parameter(property = "testFramework", required = false)
    protected var testFramework: String? = null

    @Parameter(property = "lang", required = false)
    protected var lang: String? = null

    @Parameter(property = "removeNonPassingTests", required = false)
    protected var removeNonPassingTests: Boolean = true

    @Parameter(property = "dirPrompts", required = false)
    protected var dirPrompts: String? = null

    @Parameter(property = "dirOutput", required = false)
    protected var dirOutput: String? = null

    // Env - LLM related variables
    @Parameter(property = "gptApiKey", required = false)
    protected var gptApiKey: String? = null

    @Parameter(property = "gptOrganization", required = false)
    protected var gptOrganization: String? = null

    @Parameter(property = "gptModel", required = false)
    protected var gptModel: String? = null

    @Parameter(property = "deepseekApiKey", required = false)
    protected var deepseekApiKey: String? = null

    @Parameter(property = "deepseekBaseUrl", required = false)
    protected var deepseekBaseUrl: String? = null

    @Parameter(property = "ollamaChatUrl", required = false)
    protected var ollamaChatUrl: String? = null

    // Env - Execution related variables
    @Parameter(property = "maxFixIterations", required = false)
    protected var maxFixIterations: Int? = null

    @Parameter(property = "maxGenerateIterations", required = false)
    protected var maxGenerateIterations: Int? = null

    @Parameter(property = "maxFixUsingMoreContent", required = false)
    protected var maxFixUsingMoreContent: Int? = null

    @Parameter(property = "maxFixWrongInvocations", required = false)
    protected var maxFixWrongInvocations: Int? = null

    @Parameter(property = "maxRemoveTestsIterations", required = false)
    protected var maxRemoveTestsIterations: Int? = null

    @Parameter(property = "maxRepeatFailedIterations", required = false)
    protected var maxRepeatFailedIterations: Int? = null

    @Parameter(property = "maxRepeatFailedApiIterations", required = false)
    protected var maxRepeatFailedApiIterations: Int? = null

    @Parameter(property = "maxFixOracleIterations", required = false)
    protected var maxFixOracleIterations: Int? = null

    @Parameter(property = "maxFixOracleUsingModelIterations", required = false)
    protected var maxFixOracleUsingModelIterations: Int? = null

    @Parameter(property = "requiredImports", required = false)
    protected var requiredImports: String? = null

    protected fun initialize() {
        if (repositoryPath.isEmpty()) {
            repositoryPath = Paths.get("").toAbsolutePath().toString()
        }

        if (!repositoryPath.endsWith("/")) {
            repositoryPath += "/"
        }

        setEnvValues()
        PromptService.initialize()
    }

    /**
     * Verifies the programming language is supported and instantiates a new YateRunner object
     */
    protected fun createRunner(): YateJavaRunner {
        if (lang?.uppercase() !== ProgramLangType.JAVA.toString()) {
            throw InvalidInputException("Given programming language is not supported at the moment")
        }

        YateConsole.debug("Instantiating YateJavaRunner with the following settings")
        YateConsole.debug("----> Include oracle fixing: $includeOracleFixing")
        YateConsole.debug("----> Generated test final directory: ${outputDirectory ?: "(Repository, tests folder)"}")
        YateConsole.debug("----> Model name: ${modelName ?: "(Default)"}")

        return YateJavaRunner(repositoryPath, includeOracleFixing, outputDirectory, modelName)
    }

    protected fun setEnvValues() {
        ConfigYate.initialize(config)

        if (dirPrompts != null) {
            ConfigYate.setValue("DIR_PROMPTS", dirPrompts!!)
        }
        if (dirOutput != null) {
            ConfigYate.setValue("DIR_OUTPUT", dirOutput!!)
        }
        if (testFramework != null) {
            ConfigYate.setValue("TEST_FRAMEWORK", testFramework!!)
        }
        if (lang != null) {
            ConfigYate.setValue("LANG", lang!!)
        }
        if (gptApiKey != null) {
            ConfigYate.setValue("GPT_API_KEY", gptApiKey!!)
        }
        if (gptOrganization != null) {
            ConfigYate.setValue("GPT_ORGANIZATION", gptOrganization!!)
        }
        if (gptModel != null) {
            ConfigYate.setValue("GPT_MODEL", gptModel!!)
        }
        if (deepseekApiKey != null) {
            ConfigYate.setValue("DEEPSEEK_API_KEY", deepseekApiKey!!)
        }
        if (deepseekBaseUrl != null) {
            ConfigYate.setValue("DEEPSEEK_BASE_URL", deepseekBaseUrl!!)
        }
        if (ollamaChatUrl != null) {
            ConfigYate.setValue("OLLAMA_CHAT_URL", ollamaChatUrl!!)
        }
        if (maxFixIterations != null) {
            ConfigYate.setValue("MAX_FIX_ITERATIONS", maxFixIterations!!.toString())
        }
        if (maxGenerateIterations != null) {
            ConfigYate.setValue("MAX_GENERATE_ITERATIONS", maxGenerateIterations!!.toString())
        }
        if (maxFixUsingMoreContent != null) {
            ConfigYate.setValue("MAX_FIX_USING_MORE_CONTENT", maxFixUsingMoreContent!!.toString())
        }
        if (maxFixWrongInvocations != null) {
            ConfigYate.setValue("MAX_FIX_WRONG_INVOCATIONS", maxFixWrongInvocations!!.toString())
        }
        if (maxRemoveTestsIterations != null) {
            ConfigYate.setValue("MAX_REMOVE_TESTS_ITERATIONS", maxRemoveTestsIterations!!.toString())
        }
        if (maxRepeatFailedIterations != null) {
            ConfigYate.setValue("MAX_REPEAT_FAILED_ITERATIONS", maxRepeatFailedIterations!!.toString())
        }
        if (maxRepeatFailedApiIterations != null) {
            ConfigYate.setValue("MAX_REPEAT_FAILED_API_ITERATIONS", maxRepeatFailedApiIterations!!.toString())
        }
        if (maxFixOracleIterations != null) {
            ConfigYate.setValue("MAX_FIX_ORACLE_ITERATIONS", maxFixOracleIterations!!.toString())
        }
        if (maxFixOracleUsingModelIterations != null) {
            ConfigYate.setValue("MAX_FIX_ORACLE_USING_MODEL_ITERATIONS", maxFixOracleUsingModelIterations!!.toString())
        }
        if (requiredImports != null) {
            ConfigYate.setValue("REQUIRED_IMPORTS", requiredImports!!)
        }

        ConfigYate.setValue("REMOVE_NON_PASSING_TESTS", removeNonPassingTests.toString())
    }
}