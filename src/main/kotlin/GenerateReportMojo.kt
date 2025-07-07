package com.mkonst

import com.mkonst.evaluation.EvaluationDataset
import com.mkonst.evaluation.EvaluationDatasetRecord
import com.mkonst.exceptions.InvalidInputException
import com.mkonst.helpers.YateIO
import com.mkonst.helpers.YateUtils
import com.mkonst.services.CoverageService
import com.mkonst.types.ReportType
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

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

        // todo: use cached coverages
        val coverages: Map<String, String> = CoverageService.getJacocoCoverages(repositoryPath)


        // todo: calculate mutation score

        if (type === ReportType.CONSOLE || type === ReportType.TEXT) {
            val report = if (file!== null) generateTextBasedReport(coverages, EvaluationDataset(file)) else generateTextBasedReport(coverages)

            if (type === ReportType.CONSOLE) {
                println(report)
            } else {
                YateIO.writeFile("$repositoryPath/yate_report_$key.txt", report)
            }
        }


    }

    private fun generateTextBasedReport(coverages: Map<String, String>, dataset: EvaluationDataset? = null): String {
        val report: StringBuilder = StringBuilder()
        report.appendLine("YATE - TEST REPORT")
        report.appendLine("==================\n")

        report.appendLine("Repository under test: $repositoryPath")
        report.appendLine("------------------\n")

        report.appendLine("Code coverage")
        report.appendLine("------------------")
        report.appendLine("Line coverage:  \t${coverages["line_coverage"]}")
        report.appendLine("Branch coverage:\t${coverages["branch_coverage"]}")
        report.appendLine("Method coverage:\t${coverages["method_coverage"]}")
        report.appendLine("Class coverage: \t${coverages["class_coverage"]}")
        report.appendLine("Mutation score: \t0,00%")

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
}