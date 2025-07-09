package com.mkonst

import com.mkonst.evaluation.EvaluationDataset
import com.mkonst.evaluation.EvaluationDatasetRecord
import com.mkonst.exceptions.InvalidInputException
import com.mkonst.helpers.YateConsole
import com.mkonst.helpers.YateIO
import com.mkonst.helpers.YateUtils
import com.mkonst.services.CoverageService
import com.mkonst.types.ReportType
import com.mkonst.types.coverage.JacocoCoverageHolder
import com.mkonst.types.coverage.MutationScore
import kotlinx.serialization.json.Json
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

@Mojo(name = "generateReport")
class GenerateReportMojo: AbstractYateMojo() {

    @Parameter(property = "key", required = true)
    private lateinit var key: String

    @Parameter(property = "type", required = false)
    private var type: ReportType = ReportType.CONSOLE

    @Parameter(property = "file", required = false)
    private val file: String? = null

    override fun execute() {
        initialize()

        if (file!== null && !file.endsWith(".csv")) {
            throw InvalidInputException("-Dfile argument must be a type of csv")
        }

        val coverages: JacocoCoverageHolder
        val potentialCoveragesFile = File("$repositoryPath/yate_jacoco_coverages_$key.json")
        if (potentialCoveragesFile.exists()) {
            val loadedJson = potentialCoveragesFile.readText()
            coverages = Json.decodeFromString<JacocoCoverageHolder>(loadedJson)
        } else {
            coverages = CoverageService.getJacocoCoverages(repositoryPath)
        }

        // Reading mutation score from text file (if exists)
        val mutationScore: MutationScore? = extractMutationScore("$repositoryPath/yate_ms_score_$key.txt")

        if (type === ReportType.CONSOLE || type === ReportType.TEXT) {
            val report = if (file!== null) generateTextBasedReport(coverages, mutationScore, EvaluationDataset(file)) else generateTextBasedReport(coverages)

            if (type === ReportType.CONSOLE) {
                println(report)
            } else {
                YateIO.writeFile("$repositoryPath/yate_report_$key.txt", report)
                YateConsole.info("Report generated and saved here: $repositoryPath/yate_report_$key.txt")
            }
        }


    }

    private fun generateTextBasedReport(coverages: JacocoCoverageHolder, mutationScore: MutationScore? = null, dataset: EvaluationDataset? = null): String {
        val report: StringBuilder = StringBuilder()
        report.appendLine("YATE - TEST REPORT")
        report.appendLine("==================\n")

        report.appendLine("Repository under test: $repositoryPath")
        report.appendLine("------------------\n")

        report.appendLine("Code coverage")
        report.appendLine("------------------")
        report.appendLine("Line coverage:  \t${coverages.lineCoverage.getScoreText(true)}")
        report.appendLine("Branch coverage:\t${coverages.branchCoverage.getScoreText(true)}")
        report.appendLine("Method coverage:\t${coverages.methodCoverage.getScoreText(true)}")
        report.appendLine("Class coverage: \t${coverages.classCoverage.getScoreText(true)}")

        if (mutationScore !== null) {
            report.appendLine("Mutation score: \t${mutationScore}")
        } else {
            report.appendLine("Mutation score: \tN/A")
        }


        if (dataset !== null) {
            report.appendLine("\nPerformance (Total)")
            report.appendLine("------------------")
            report.appendLine(dataset.getTotalsText())

            report.appendLine("\nPerformance (Per File)")
            report.appendLine("------------------")
            for (record in dataset.records) {
                report.append(getDatasetRowText(record))
            }
        }

        return report.toString()
    }

    private fun getDatasetRowText(row: EvaluationDatasetRecord): String {
        val output = StringBuilder()
        output.appendLine("\n*******************")
        output.appendLine(row.classPath)
        output.appendLine("*******************")
        output.appendLine("Execution status: ${if (row.isExecuted) "Success" else "Failed"}")
        output.appendLine("Generation time: ${row.generationTime}")
        output.appendLine("Generation time (human readable): ${YateUtils.formatMillisToMinSec(row.generationTime)}")
        output.appendLine("#Total requests: ${row.requests.total}")
        output.appendLine("#Generation requests: ${row.requests.generation}")
        output.appendLine("#Compilation fixing requests: ${row.requests.compilationFixing}")
        output.appendLine("#Oracle fixing requests: ${row.requests.oracleFixing}")
        output.appendLine("#Coverage enhancement requests: ${row.requests.coverageEnhancement}")

        return output.toString()
    }

    private fun extractMutationScore(filepath: String): MutationScore? {
        if (!File(filepath).exists()) {
            return null
        }

        val content = File(filepath).readText()
        val regex = Regex("""\((\d+)\s*/\s*(\d+)\)""")
        val match = regex.find(content)
        return match?.let {
            val first = it.groupValues[1].toInt()
            val second = it.groupValues[2].toInt()
            MutationScore(second, first)
        }
    }
}