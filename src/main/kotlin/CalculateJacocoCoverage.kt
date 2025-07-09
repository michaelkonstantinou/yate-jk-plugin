package com.mkonst

import com.mkonst.helpers.YateConsole
import com.mkonst.helpers.YateIO
import com.mkonst.helpers.YateUtils
import com.mkonst.services.CoverageService
import com.mkonst.services.PiTestService
import com.mkonst.types.TestLevel
import com.mkonst.types.coverage.JacocoCoverageHolder
import com.mkonst.types.coverage.MutationScore
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

@Mojo(name = "calculateJacocoCoverage")
class CalculateJacocoCoverage: AbstractYateMojo() {

    @Parameter(property = "key", required = false)
    private var key: String? = null

    override fun execute() {
        initialize()

        val dependencyTool = YateUtils.getDependencyTool(repositoryPath)
        println("The given repository is using ${dependencyTool.name}")

        val coverages: JacocoCoverageHolder = CoverageService.getJacocoCoverages(repositoryPath)
        YateConsole.info("Calculated coverage based on jacoco")
        println(coverages)

        if (key !== null) {
            val jsonString = Json.encodeToString(coverages)
            File("$repositoryPath/yate_jacoco_coverages_$key.json").writeText(jsonString)
        }
    }
}