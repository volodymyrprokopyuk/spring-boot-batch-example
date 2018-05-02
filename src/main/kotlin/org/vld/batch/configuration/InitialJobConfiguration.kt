package org.vld.batch.configuration

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.LineMapper
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor
import org.springframework.batch.item.file.transform.DelimitedLineAggregator
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.FieldExtractor
import org.springframework.batch.item.file.transform.LineAggregator
import org.springframework.batch.item.file.transform.LineTokenizer
import org.springframework.batch.item.file.transform.PassThroughLineAggregator
import org.springframework.batch.item.file.transform.RegexLineTokenizer
import org.springframework.batch.item.support.ClassifierCompositeItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.vld.batch.domain.Female
import org.vld.batch.domain.FemaleBegin
import org.vld.batch.domain.FemaleContact
import org.vld.batch.domain.FemaleEnd
import org.vld.batch.domain.FemaleName
import org.vld.batch.domain.Human
import org.vld.batch.domain.HumanItemBuilderClassifier
import org.vld.batch.domain.HumanLine
import org.vld.batch.domain.Male
import org.vld.batch.domain.MaleBegin
import org.vld.batch.domain.MaleContact
import org.vld.batch.domain.MaleEnd
import org.vld.batch.domain.MaleName
import org.vld.batch.domain.Person
import org.vld.batch.listener.SimpleJobExecutionListener
import org.vld.batch.processor.UpperCasePeopleProcessor
import org.vld.batch.reader.AggregateItemReader
import org.vld.batch.tasklet.JobIdentificationTasklet
import javax.sql.DataSource

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
