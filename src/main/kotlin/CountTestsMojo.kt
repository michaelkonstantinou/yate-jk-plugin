package com.mkonst

import com.mkonst.exceptions.InvalidInputException
import com.mkonst.helpers.TestFileHelper
import com.mkonst.helpers.YateCodeUtils
import com.mkonst.helpers.YateJavaUtils
import com.mkonst.helpers.YateUtils
import org.apache.maven.plugins.annotations.Mojo
import java.io.File


@Mojo(name = "countTests")
class CountTestsMojo: AbstractYateMojo() {

    override fun execute() {
        initialize()
        val nrTests = TestFileHelper.getNumberOfTestsInRepository(repositoryPath)

        println("Number of tests: #$nrTests")
    }
}