package com.mkonst

import com.mkonst.exceptions.InvalidInputException
import com.mkonst.helpers.YateCodeUtils
import com.mkonst.helpers.YateJavaUtils
import com.mkonst.helpers.YateUtils
import org.apache.maven.plugins.annotations.Mojo
import java.io.File


@Mojo(name = "countTests")
class CountTestsMojo: AbstractYateMojo() {

    override fun execute() {
        initialize()

        var nrTests = 0
        val repoRoot = File(this.repositoryPath)
        repoRoot.walk()

            // todo: support .kt files as well
            .filter { it.isFile && it.nameWithoutExtension.endsWith("Test") && it.extension == "java" }
            .forEach { file ->
                println(file)
                var tests = 0
                try {
                    tests = YateJavaUtils.countTestMethods(file.absolutePath)
                } catch (e: Exception) {
                    tests = YateJavaUtils.countTestAnnotations(file.absolutePath)
                }

                nrTests += tests
            }

        println("Number of tests: #$nrTests")
    }
}