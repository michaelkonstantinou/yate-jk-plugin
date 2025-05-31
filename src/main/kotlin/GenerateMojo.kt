package com.mkonst

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo

@Mojo(name = "generate")
class GenerateMojo: AbstractMojo() {

    override fun execute() {
        println("Generation process executed")
    }
}