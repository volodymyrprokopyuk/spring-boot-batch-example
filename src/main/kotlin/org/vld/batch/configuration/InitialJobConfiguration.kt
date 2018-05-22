package org.vld.batch.configuration

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.vld.batch.listener.SimpleJobExecutionListener
import org.vld.batch.tasklet.JobIdentificationTasklet

@Configuration
@EnableBatchProcessing
open class InitialJobConfiguration {

    @Autowired
    private lateinit var jobBuilderFactory: JobBuilderFactory

    @Autowired
    private lateinit var stepBuilderFactory: StepBuilderFactory

    // ** initialJob
    @Bean
    open fun initialJob(): Job = jobBuilderFactory.get("initialJob")
            .incrementer(RunIdIncrementer())
            .listener(SimpleJobExecutionListener())
            .start(jobIdentificationStep())
            .build()

    // jobIdentificationStep
    @Bean
    open fun jobIdentificationStep(): Step = stepBuilderFactory.get("jobIdentificationStep")
            .tasklet(JobIdentificationTasklet())
            .build()
}
