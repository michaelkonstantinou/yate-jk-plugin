package com.mkonst

import com.mkonst.exceptions.InvalidInputException
import com.mkonst.helpers.YateCodeUtils
import com.mkonst.helpers.YateConsole
import com.mkonst.helpers.YateIO
import com.mkonst.helpers.YateUtils
import com.mkonst.services.ErrorService
import com.mkonst.types.DependencyTool
import com.mkonst.types.ProgramLangType
import com.mkonst.types.TestLevel
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

@Mojo(name = "removeFailingTests")
class RemoveFailingTestsMojo: AbstractYateMojo() {

    @Parameter(property = "type", required = false, defaultValue = "all")
    private lateinit var type: String

    @Parameter(property = "maxIterations", required = false)
    private var maxIterations: Int = 10

    override fun execute() {
        initialize()

        // Validate input
        if (type != "all" && type != "non-compile" && type != "non-passing") {
            throw InvalidInputException("Type of tests to remove must be 'all', 'non-compile' or 'non-passing'")
        }

        val dependencyTool: DependencyTool = YateUtils.getDependencyTool(repositoryPath)
        val errorService = ErrorService(repositoryPath)

        // Get all test filepaths associated by their class name
        val classPathsByName: Map<String, String> = getClassPathsByName()
        var totalRemovedTests: Int = 0

        // Remove tests that do not compile
        if(type == "all" || type == "non-compile") {
            YateConsole.warning("Non-compiling tests are not always possible to remove. This functionality is in beta version")

            var nrIncorrectImports = 0
            var nrIncorrectFiles = 0
            for (i in 1..maxIterations) {
                YateConsole.info("Looking for files with compilation issues. Iteration #$i")
                val (invalidMethodsByFile, invalidImportsByFile, invalidStaticClassesByFile) = errorService.findNonCompilingClassesRegex(dependencyTool)

                if (invalidMethodsByFile.isEmpty()) {
                    YateConsole.info("No more invalid methods found to remove")
                }

                // Remove invalid methods
                for ((testClassPath, invalidMethods) in invalidMethodsByFile) {
                    totalRemovedTests += invalidMethods.size
                    var content: String = YateIO.readFile(testClassPath)

                    for (method in invalidMethods) {
                        content = content.replace(method, "")
                    }

                    YateConsole.debug("Removing #${invalidMethods.size} methods in file $testClassPath")
                    YateIO.writeFile(testClassPath, content)
                }

                if (invalidImportsByFile.isEmpty()) {
                    YateConsole.info("No invalid files found due to import statements")
                }

                // Remove files with incorrect import statements
                for((testClassPath, invalidImports) in invalidImportsByFile) {
                    YateConsole.debug("$testClassPath: Contains ${invalidImports.size} invalid import statements")
                    nrIncorrectImports += invalidImports.size
                    nrIncorrectFiles += 1
                    YateIO.deleteFile(testClassPath)
                }

                // Remove invalid static classes
                for ((testClassPath, invalidStaticClasses) in invalidStaticClassesByFile) {
                    var content: String = YateIO.readFile(testClassPath)
                    for (method in invalidStaticClasses) {
                        content = content.replace(method, "")
                    }

                    YateConsole.debug("Removing #${invalidStaticClasses.size} classes in file $testClassPath")
                    YateIO.writeFile(testClassPath, content)
                }

                if (invalidImportsByFile.isEmpty() && invalidMethodsByFile.isEmpty() && invalidStaticClassesByFile.isEmpty()) {
                    break
                }
            }

            YateConsole.info("Total wrong import statements: $nrIncorrectImports")
            YateConsole.info("Total removed files due to wrong import statements: $nrIncorrectFiles")
            YateConsole.info("Total removed methods from log (could be also in the import statement files): $totalRemovedTests")
        }

        // Remove tests that do not compile
        if(type == "all" || type == "non-passing") {
            for (i in 1..maxIterations) {
                val nonPassingTests = errorService.findNonPassingTests(dependencyTool)
                if (nonPassingTests.isEmpty()) {
                    YateConsole.info("No more tests to remove")
                    break
                }

                // Remove non passing tests and re-write the test class file
                for((testClassName, invalidTests) in nonPassingTests) {
                    if (testClassName.equals("com") || testClassName.equals("org") || testClassName.equals("software") || testClassName.equals("amazon")) {
                        YateConsole.error("Invalid class name found '$testClassName' with the following invalid tests: $invalidTests")
                        continue
                    }

                    YateConsole.debug("$testClassName: ${invalidTests.size} tests must be removed as they do not pass")
                    val newContent: String = YateCodeUtils.removeMethodsInClass(classPathsByName[testClassName]!!, invalidTests, ProgramLangType.JAVA)
                    YateIO.writeFile(classPathsByName[testClassName]!!, newContent)
                    totalRemovedTests += invalidTests.size
                }
            }
        }

        YateConsole.info("$totalRemovedTests tests have been removed in total")
    }

    private fun getClassPathsByName(): Map<String, String> {
        val testDirectory = File(repositoryPath + "src/test/")

        return testDirectory.walkTopDown()
            .filter { it.isFile && (it.extension == "java" || it.extension == "kt") }
            .filter { it.nameWithoutExtension.endsWith("Test") }
            .associate { it.nameWithoutExtension to it.absolutePath }
    }
}