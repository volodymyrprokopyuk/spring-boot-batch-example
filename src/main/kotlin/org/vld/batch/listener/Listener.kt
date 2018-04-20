package org.vld.batch.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener

class SimpleJobExecutionListener : JobExecutionListener {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(SimpleJobExecutionListener::class.java)
    }

    override fun beforeJob(jobExecution: JobExecution?) {
        logger.info("** Before Job")
    }

    override fun afterJob(jobExecution: JobExecution?) {
        logger.info("** After Job")
        if (jobExecution?.status != BatchStatus.COMPLETED) {
            val failureException = jobExecution?.failureExceptions?.first()
            val exitCode = when (failureException) {
                is IllegalArgumentException -> 1
                else -> -1
            }
            System.exit(exitCode)
        }
    }
}