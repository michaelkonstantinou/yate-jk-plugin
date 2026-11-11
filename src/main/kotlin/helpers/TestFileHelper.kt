package com.mkonst.helpers

import java.io.File

object TestFileHelper {

    fun getNumberOfTestsInRepository(repositoryPath: String): Int {
        var nrTests = 0
        val repoRoot = File(repositoryPath)
        repoRoot.walk()

            // todo: support .kt files as well
            .filter { it.isFile && it.extension == "java" && (it.nameWithoutExtension.endsWith("Test") || (it.absolutePath.contains("src/test") && it.nameWithoutExtension.startsWith("Test"))) }
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

        return nrTests
    }
}