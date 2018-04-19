package org.vld.batch.configuration

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.LineMapper
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.batch.item.file.transform.LineTokenizer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.vld.batch.domain.Person
import org.vld.batch.listener.SimpleJobExecutionListener
import org.vld.batch.tasklet.JobIdentificationTasklet
import javax.sql.DataSource

@Configuration
@EnableBatchProcessing
open class ApplicationConfiguration {

    @Autowired
    private lateinit var jobBuilderFactory: JobBuilderFactory

    @Autowired
    private lateinit var stepBuilderFactory: StepBuilderFactory

    @Autowired
    private lateinit var dataSource: DataSource

    // initialJob
    @Bean
    open fun initialJob(): Job = jobBuilderFactory.get("initialJob")
            .incrementer(RunIdIncrementer())
            .listener(SimpleJobExecutionListener())
            .start(jobIdentificationStep())
            .build()

    @Bean
    open fun jobIdentificationStep(): Step = stepBuilderFactory.get("jobIdentificationStep")
            .tasklet(JobIdentificationTasklet())
            .build()

    // importPeopleJob
    @Bean
    open fun importPeopleJob(): Job = jobBuilderFactory.get("importPeopleJob")
            .incrementer(RunIdIncrementer())
            .start(importPeopleStep())
            .build()

    @Bean
    open fun importPeopleStep(): Step = stepBuilderFactory.get("importPeopleStep")
            .chunk<Person, Person>(1)
            .reader(importPeopleReader())
            .writer(importPeopleWriter())
            .build()

    @Bean
    open fun importPeopleReader(): ItemReader<Person> {
        val reader = FlatFileItemReader<Person>()
        reader.setResource(FileSystemResource("data/people.txt"))
        reader.setLineMapper(importPeopleLineMapper())
        return reader
    }

    @Bean
    open fun importPeopleLineMapper(): LineMapper<Person> {
        val lineMapper = DefaultLineMapper<Person>()
        lineMapper.setLineTokenizer(importPeopleLineTokenizer())
        lineMapper.setFieldSetMapper(importPeopleFieldSetMapper())
        return lineMapper
    }

    @Bean
    open fun importPeopleLineTokenizer(): LineTokenizer {
        val lineTokenizer = DelimitedLineTokenizer()
        lineTokenizer.setDelimiter(",")
        lineTokenizer.setNames(arrayOf("firstName", "lastName"))
        return lineTokenizer
    }

    @Bean
    open fun importPeopleFieldSetMapper(): FieldSetMapper<Person> {
        val fieldSetMapper = BeanWrapperFieldSetMapper<Person>()
        fieldSetMapper.setTargetType(Person::class.java)
        return fieldSetMapper
    }

    @Bean
    open fun importPeopleWriter(): ItemWriter<Person> {
        val writer = JdbcBatchItemWriter<Person>()
        writer.setDataSource(dataSource)
        writer.setItemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider<Person>())
        writer.setSql("INSERT INTO family.person(first_name, last_name) VALUES (:firstName, :lastName)")
        return writer
    }

    // exportPeopleJob
}
