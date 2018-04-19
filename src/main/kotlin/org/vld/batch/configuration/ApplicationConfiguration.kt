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
open class ApplicationConfiguration {

    @Autowired
    private lateinit var jobBuilderFactory: JobBuilderFactory

    @Autowired
    private lateinit var stepBuilderFactory: StepBuilderFactory

    // simpleJob
    @Bean
    open fun simpleJob(): Job = jobBuilderFactory.get("simpleJob")
            .incrementer(RunIdIncrementer())
            .listener(SimpleJobExecutionListener())
            .start(simpleStep())
            .build()

    @Bean
    open fun simpleStep(): Step = stepBuilderFactory.get("simpleStep")
            .tasklet(JobIdentificationTasklet())
            .build()
}
