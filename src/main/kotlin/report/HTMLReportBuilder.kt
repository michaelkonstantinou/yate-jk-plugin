package com.mkonst.report

import com.mkonst.evaluation.EvaluationDataset
import com.mkonst.evaluation.EvaluationDatasetRecord
import com.mkonst.helpers.YateIO
import com.mkonst.helpers.YateUtils
import com.mkonst.types.coverage.JacocoCoverageHolder
import com.mkonst.types.coverage.MutationScore

class HTMLReportBuilder {
    private var coverages: JacocoCoverageHolder? = null
    private var msScore: MutationScore? = null
    private var evaluationDataset: EvaluationDataset? = null

    fun appendEvaluationDataset(filepath: String): HTMLReportBuilder {
        this.evaluationDataset = EvaluationDataset(filepath)

        return this
    }

    fun appendJacocoCoverages(coverages: JacocoCoverageHolder): HTMLReportBuilder
    {
        this.coverages = coverages.copy()

        return this
    }

    fun appendMutationScore(msScore: MutationScore?): HTMLReportBuilder
    {
        this.msScore = msScore?.copy()

        return this
    }

    fun generate(outputFile: String)
    {
        var content = object {}.javaClass
            .getResourceAsStream("/report_placeholder.html")
            ?.bufferedReader()
            ?.readText()
            ?: error("report_placeholder.html not found in resources")
        content = this.generateCoveragesSection(content)
        content = this.generatePerformanceSection(content)
        content = this.generatePerformancePerFileSection(content)

        YateIO.writeFile(outputFile, content)
    }

    private fun generateCoveragesSection(content: String): String
    {
        if (this.coverages === null) {
            return content
        }

        return content.replace("%%LINE_COVERAGE%%", this.coverages!!.lineCoverage.getScoreText(true))
            .replace("%%BRANCH_COVERAGE%%", this.coverages!!.branchCoverage.getScoreText(true))
            .replace("%%METHOD_COVERAGE%%", this.coverages!!.methodCoverage.getScoreText(true))
            .replace("%%CLASS_COVERAGE%%", this.coverages!!.classCoverage.getScoreText(true))
            .replace("%%MUTATION_SCORE%%", this.msScore?.toString() ?: "N/A")
    }

    /**
     * Uses the EvaluationDataset total values and generates the input of the Performance Section to show the totals
     * as a list
     */
    private fun generatePerformanceSection(content: String): String
    {
        if (this.evaluationDataset === null) {
            return content
        }

        val totals = this.evaluationDataset!!.getTotals()
        val output: StringBuilder = StringBuilder()
        output.append(generatePerformanceRow("Total Requests", totals["totalRequests"].toString()))
        output.append(generatePerformanceRow("Total Generation Requests", totals["totalGenerationRequests"].toString()))
        output.append(generatePerformanceRow("Total Compiling fixing Requests", totals["totalCompilingFixingRequests"].toString()))
        output.append(generatePerformanceRow("Total Oracle fixing Requests", totals["totalOracleFixingRequests"].toString()))
        output.append(generatePerformanceRow("Total Coverage enhancement Requests", totals["totalCoverageEnhanceRequests"].toString()))
        output.append(generatePerformanceRow("Total Generated Tests", totals["totalGeneratedTests"].toString()))
        output.append(generatePerformanceRow("Total Generation Time", totals["totalGenerationTime"].toString()))
        output.append(generatePerformanceRow("Total Generation Time (human readable)", YateUtils.formatMillisToMinSec(totals["totalGenerationTime"] as Long)))
        output.append(generatePerformanceRow("Average Nr. Requests", YateUtils.formatDecimal(totals["avgRequests"] as Float)))
        output.append(generatePerformanceRow("Average Generation Time", YateUtils.formatDecimal(totals["avgGenerationTime"] as Float)))

        return content.replace("%%PERFORMANCE_VALUES%%", output.toString())
    }

    private fun generatePerformancePerFileSection(content: String): String {
        if (this.evaluationDataset === null) {
            return content
        }

        val tableRows = StringBuilder()
        for (record in this.evaluationDataset!!.records) {
            tableRows.append(generatePerformancePerFileRow(record))
        }

        return content.replace("%%PERFORMANCE_ROWS%%", tableRows.toString())
    }

    private fun generatePerformancePerFileRow(row: EvaluationDatasetRecord): String
    {
        return "<tr><td>${row.classPath}</td>\n" +
                "<td class=\"${if (row.isExecuted) "success" else "failure"}\">${if (row.isExecuted) "Success" else "Failed"}</td>" +
                "<td>${row.requests.total}</td>" +
                "<td>${row.requests.generation}</td>" +
                "<td>${row.requests.compilationFixing}</td>" +
                "<td>${row.requests.oracleFixing}</td>" +
                "<td>${row.requests.coverageEnhancement}</td>" +
                "<td>${row.generationTime}</td>" +
                "<td>${YateUtils.formatMillisToMinSec(row.generationTime)}</td>" +
                "</tr>"
    }

    private fun generatePerformanceRow(text: String, value: String): String
    {
        return "<li>$text: <span class=\"tag\">$value</span></li>"
    }
}