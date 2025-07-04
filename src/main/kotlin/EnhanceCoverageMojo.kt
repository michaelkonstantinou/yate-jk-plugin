package com.mkonst

import com.mkonst.exceptions.InvalidInputException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

@Mojo(name = "enhanceCoverage")
class EnhanceCoverageMojo: AbstractYateMojo() {

    @Parameter(property = "type", required = false, defaultValue = "line")
    private lateinit var type: String

    override fun execute() {
        initialize()
        validateInput()

        // todo: iterate all test files, estimate the class under test, and then use the coverageEnhancer
    }

    private fun validateInput() {
        if (type !== "line" && type !== "branch") {
            throw InvalidInputException("Type must be line or branch")
        }
    }
}