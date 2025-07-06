package com.mkonst

import com.mkonst.helpers.YateIO
import com.mkonst.helpers.YateUtils
import com.mkonst.services.PiTestService
import com.mkonst.types.TestLevel
import com.mkonst.types.coverage.MutationScore
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

@Mojo(name = "calculateMutationScore")
class CalculateMutationScoreMojo: AbstractYateMojo() {

    @Parameter(property = "key", required = false)
    private var key: String? = null

    @Parameter(property = "failOnError", required = false)
    private var failOnError: Boolean = false

    override fun execute() {
        initialize()

        val dependencyTool = YateUtils.getDependencyTool(repositoryPath)
        println("The given repository is using ${dependencyTool.name}")

        // todo: Check for cached files

        // Run mutation score using Pi-Test
        val piTestService: PiTestService = PiTestService(repositoryPath)
        val msScore: MutationScore = piTestService.runMutationScore(dependencyTool)

        if (key !== null) {
            YateIO.writeFile("$repositoryPath/yate_ms_score_$key.txt", msScore.toString())
        }
    }
}