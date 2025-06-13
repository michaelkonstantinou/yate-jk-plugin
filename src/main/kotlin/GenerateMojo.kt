package com.mkonst

import com.mkonst.config.ConfigYate
import com.mkonst.evaluation.RequestsCounter
import com.mkonst.helpers.YateConsole
import com.mkonst.helpers.YateJavaUtils.countTestMethods
import com.mkonst.runners.YateJavaRunner
import com.mkonst.types.TestLevel
import com.sun.org.apache.xpath.internal.operations.Bool
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.jvm.Throws

@Mojo(name = "generate")
class GenerateMojo: AbstractYateMojo() {

    @Parameter(property = "type", required = false, defaultValue = "CLASS")
    private var testGenerationType: TestLevel = TestLevel.CLASS

    @Parameter(property = "classPath", required = true)
    private lateinit var classPath: String

    override fun execute() {
        initialize()
        validateInput()

        YateConsole.info("Current repository path: $repositoryPath")

        // Generation process
        val runner: YateJavaRunner = createRunner()

        try {
            YateConsole.info("Generating tests for class $classPath")
            val startTime = System.currentTimeMillis()
            val responses = runner.generate(classPath, testGenerationType)
            val endTime = System.currentTimeMillis()

            if (responses.isEmpty()) {
                YateConsole.warning("Unable to generate tests")
            } else {
                YateConsole.info("Generation process finished for $classPath")

                var generatedTests: Int = 0
                for ((testClassContainer) in responses) {
                    generatedTests += countTestMethods(testClassContainer)
                }

                val requestsCounter: RequestsCounter = runner.getNrRequests()
                println("# Tests: $generatedTests")
                println("# All Requests: ${requestsCounter.total}")
                println("# Generation Requests: ${requestsCounter.generation}")
                println("# Compilation fixing Requests: ${requestsCounter.compilationFixing}")
                println("# Oracle fixing Requests: ${requestsCounter.oracleFixing}")
                println("# Coverage enhancement Requests: ${requestsCounter.coverageEnhancement}")
                println("Generation time: ${startTime - endTime}")
            }

        } catch (e: Exception) {
            YateConsole.error("Error when running the generation process of the runner. Exception thrown: ${e.javaClass}")
            YateConsole.error(e.message ?: "(No message available in exception)")
        }

        runner.close()
    }

    @Throws
    private fun validateInput() {
        if (classPath.trim().isEmpty()) {
            throw Exception("Class path property cannot be empty")
        }

        var pathUnderValidation = Paths.get(classPath)
        if (!Files.exists(pathUnderValidation)) {
            throw Exception("Class path $classPath does not exist")
        }

        pathUnderValidation = Paths.get(repositoryPath)
        if (!Files.exists(pathUnderValidation)) {
            throw Exception("Repository path $repositoryPath does not exist")
        }
    }
}