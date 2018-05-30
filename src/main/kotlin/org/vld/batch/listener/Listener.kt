package org.vld.batch.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ExitCodeGenerator

class SimpleJobExecutionListener : JobExecutionListener {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(SimpleJobExecutionListener::class.java)
    }

    @Autowired
    private lateinit var jobExitCodeGenerator: JobExitCodeGenerator

    override fun beforeJob(jobExecution: JobExecution?) {
        logger.info("** Before Job")
        jobExitCodeGenerator.jobExecution = jobExecution
    }

    override fun afterJob(jobExecution: JobExecution?) {
        logger.info("** After Job")
    }
}

class JobExitCodeGenerator : ExitCodeGenerator {

    var jobExecution: JobExecution? = null

    override fun getExitCode(): Int {
        return if (jobExecution?.status != BatchStatus.COMPLETED) {
            if (jobExecution?.allFailureExceptions?.isNotEmpty() ?: false) {
                val jobException = jobExecution?.allFailureExceptions?.first()
                when (jobException) {
                    is IllegalArgumentException -> 11
                    is Exception -> 12
                    else -> 1
                }
            } else 1
        } else 0
    }
}