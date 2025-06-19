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

            for (i in 1..maxIterations) {
                val nonCompilingTests = errorService.findNonCompilingTests(dependencyTool)
                if (nonCompilingTests.isEmpty()) {
                    YateConsole.info("No more tests to remove")
                    break
                }

                // Remove non compiling tests and re-write the test class file
                for((testClassName, invalidTests) in nonCompilingTests) {
                    YateConsole.debug("$testClassName: ${invalidTests.size} tests must be removed as they do not compile")
                    val newContent: String = YateCodeUtils.removeMethodsInClass(classPathsByName[testClassName]!!, invalidTests, ProgramLangType.JAVA)
                    YateIO.writeFile(classPathsByName[testClassName]!!, newContent)
                    totalRemovedTests += invalidTests.size
                }
            }
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