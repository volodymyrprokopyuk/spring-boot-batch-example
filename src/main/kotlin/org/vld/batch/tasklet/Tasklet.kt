package org.vld.batch.tasklet

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus

class JobIdentificationTasklet : Tasklet {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(JobIdentificationTasklet::class.java)
    }

    override fun execute(contribution: StepContribution?, chunkContext: ChunkContext?): RepeatStatus {
        val jobExecution = chunkContext?.stepContext?.stepExecution?.jobExecution
        val jobName = jobExecution?.jobInstance?.jobName
        val jobInstanceId = jobExecution?.jobInstance?.id
        val jobExecutionId = jobExecution?.id
        logger.info("TASKLET: jobName = $jobName, jobInstanceId = $jobInstanceId, jobExecutionId = $jobExecutionId")
        logger.info("VERSION: ${this.javaClass.`package`.implementationTitle} ${this.javaClass.`package`.implementationVersion}")
//        throw IllegalArgumentException("oh")
//        throw Exception("oh")
        return RepeatStatus.FINISHED
    }
}