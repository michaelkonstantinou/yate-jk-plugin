package com.mkonst

import com.mkonst.config.ConfigYate.getInteger
import com.mkonst.evaluation.EvaluationDataset
import com.mkonst.evaluation.YatePlainRunner
import com.mkonst.evaluation.ablation.YateAblationRunner
import com.mkonst.exceptions.InvalidInputException
import com.mkonst.helpers.YateConsole
import com.mkonst.helpers.YateConsole.info
import com.mkonst.helpers.YateIO
import com.mkonst.helpers.YateJavaUtils.countTestMethods
import com.mkonst.helpers.YateUtils.timestamp
import com.mkonst.runners.YateAbstractRunner
import com.mkonst.runners.YateJavaRunner
import com.mkonst.runners.YateOnlyGenerationRunner
import com.mkonst.types.AblationSetup
import com.mkonst.types.TestExecutionRunner
import com.mkonst.types.TestLevel
import com.mkonst.types.YateResponse
import com.sun.org.apache.xpath.internal.operations.Bool
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.nio.file.Files
import kotlin.io.path.Path

@Mojo(name = "generateUsingDataset")
class GenerateUsingDataset: AbstractYateMojo() {
    @Parameter(property = "file", required = true)
    private lateinit var csvFile: String

    /**
     * FIXME: Evaluation only. This is used to specify which directory to copy class files from
     */
    @Parameter(property = "inputDirectory", required = false)
    private var inputDirectory: String = ""

    @Parameter(property = "ablationSetup", required = false, defaultValue = "NO_ABLATION")
    private var ablationSetting: AblationSetup = AblationSetup.NO_ABLATION

    override fun execute() {
        if (!csvFile.endsWith(".csv")) {
            throw InvalidInputException("File (-Dfile) must be of type csv")
        }

        initialize()

        val dataset = EvaluationDataset(csvFile)
        val model = this.modelName ?: dataset.records[0].modelName
        val dirOutput = this.outputDirectory ?: dataset.records[0].outputDir
        val repositoryPath: String = dataset.records[0].repositoryPath
        val recordSize = dataset.records.size
        var index = 0

        YateConsole.debug("Using csv file for generating tests:\t $csvFile")
        YateConsole.debug("Model name:\t $model")
        YateConsole.debug("Dataset size:\t #$recordSize records")

        // Set ablation setting
        val runner: YateAbstractRunner
        if (ablationSetting === AblationSetup.NO_ABLATION) {
            if (this.runnerName == TestExecutionRunner.DEFAULT) {
                runner = YateJavaRunner(dataset.records[0].repositoryPath, includeOracleFixing, dataset.records[0].outputDir, model)
            } else if (this.runnerName == TestExecutionRunner.ONLY_GENERATION) {
                runner = YateOnlyGenerationRunner(dataset.records[0].repositoryPath, dirOutput, model)
            } else {
                runner = YatePlainRunner(dataset.records[0].repositoryPath, dirOutput, model, 5)
            }

        } else {
            runner = getRunnerBasedOnAblationSetup()
        }

        for (record in dataset.records) {
            index += 1

            // Verify that the record has not been executed
            if (record.isExecuted) {
                continue
            }

            var testLevelToExecute: TestLevel = record.testLevel
            var newTestPath: String? = null
            // Copy class-level test if is HYBRID approach
            if (record.testLevel === TestLevel.HYBRID) {
                val fileAfterDirectory: String = record.classPath.substringAfter("/src/main")

                // Find source path of the class-level test
                val classLevelTest: String = Path(inputDirectory, fileAfterDirectory.replace(".java", "Test.java")).toString()
                if (!Files.exists(Path(classLevelTest))) {
                    println("File does not exist: $classLevelTest")

                    // No coverage to isolate for HYBRID version. Method-level must be used on all methods
                    testLevelToExecute = TestLevel.METHOD
                } else {
                    val targetDirectory: String = YateIO.getFolderFromPath(record.classPath.replace("/src/main", "/src/test"))
                    val newPath = YateIO.copyFileToDirectory(classLevelTest, targetDirectory)

                    if (newPath !== null) {
                        YateConsole.info("Generated test file has been moved. New path: $newPath")
                        newTestPath = newPath
                    }
                }
            }


            var hasFailed = true
            var i = 0
            while (hasFailed && i < getInteger("MAX_REPEAT_FAILED_ITERATIONS")) {
                i++

                println("Iterating class (" + index + "/" + recordSize + ") (#" + i + "): " + record.classPath)
                val startTime = System.currentTimeMillis()

                try {
                    val responses: List<YateResponse> = runner.generate(record.classPath, testLevelToExecute)

                    if (responses.isEmpty()) {
                        hasFailed = true
                        runner.resetNrRequests()

                        continue
                    }

                    // Everything went smoothly, update stats
                    record.isExecuted = true
                    record.requests = runner.getNrRequests()

                    for ((testClassContainer) in responses) {
                        val generatedTests = countTestMethods(testClassContainer)
                        if (generatedTests <= 0) {
                            throw Exception("Failed to generate tests. Re-run")
                        }
                        record.addGeneratedTests(generatedTests)
                    }
                    hasFailed = false
                } catch (e: Exception) {
                    record.errors = e.message
                    hasFailed = true
                }

                val endTime = System.currentTimeMillis()
                record.generationTime = endTime - startTime
                runner.resetNrRequests()
            }

            // Move class test to the output directory
            if (newTestPath !== null) {
                val directoriesAfterRepository: String = YateIO.getFolderFromPath(newTestPath.substringAfter("src/test"))
                val newDir = dirOutput + directoriesAfterRepository
                val newPath = YateIO.moveFileToDirectory(newTestPath, newDir)

                if (newPath !== null) {
                    YateConsole.info("Moving class file back. New path: $newPath")
                }
            }

            info("Updating dataset file")
            dataset.saveAs(csvFile)
        }

        runner.close()

        info("Saving a new dataset file by the name: ")
        val newCsvFile: String = csvFile.replace(".csv", "_results_" + timestamp() + ".csv")
        dataset.saveAs(newCsvFile)
        dataset.printTotals()
    }

    private fun getRunnerBasedOnAblationSetup(): YateAblationRunner {
        return when (ablationSetting) {
            AblationSetup.NO_SUMMARY -> YateAblationRunner(repositoryPath, includeOracleFixing, outputDirectory, modelName, false)
            AblationSetup.NO_COMPILATION_FIXING -> YateAblationRunner(repositoryPath, includeOracleFixing, outputDirectory, modelName, true, false)
            AblationSetup.NO_ORACLE_FIXING -> YateAblationRunner(repositoryPath, includeOracleFixing, outputDirectory, modelName, true, true, false)
            AblationSetup.NO_FIXING -> YateAblationRunner(repositoryPath, includeOracleFixing, outputDirectory, modelName, true, false, false)
            AblationSetup.NO_COVERAGE_ENHANCEMENT -> YateAblationRunner(repositoryPath, includeOracleFixing, outputDirectory, modelName, true, true, true, false)
            AblationSetup.NO_ABLATION -> throw Exception("Unexpected error")
        }
    }
}