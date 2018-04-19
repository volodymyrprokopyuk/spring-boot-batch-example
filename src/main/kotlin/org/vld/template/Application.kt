package org.vld.template

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.vld.template.configuration.ApplicationConfiguration

@SpringBootApplication
open class Application : CommandLineRunner {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ApplicationConfiguration::class.java)
    }

    override fun run(vararg args: String?) {
        logger.debug("ok")
    }
}

fun main(args: Array<String>) = SpringApplication.run(Application::class.java, *args)
