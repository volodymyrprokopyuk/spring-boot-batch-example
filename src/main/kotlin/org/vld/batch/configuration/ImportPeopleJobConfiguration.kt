package org.vld.batch.configuration

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.vld.batch.domain.Person
import org.vld.batch.processor.UpperCasePeopleProcessor
import javax.sql.DataSource

@Configuration
@EnableBatchProcessing
open class ImportPeopleJobConfiguration {

    @Autowired
    private lateinit var dataSource: DataSource

    @Autowired
    private lateinit var jobBuilderFactory: JobBuilderFactory

    @Autowired
    private lateinit var stepBuilderFactory: StepBuilderFactory

    // ** importPeopleJob
    @Bean
    open fun importPeopleJob(): Job = jobBuilderFactory.get("importPeopleJob")
            .incrementer(RunIdIncrementer())
            .start(importPeopleStep())
            .build()

    // importPeopleStep
    @Bean
    open fun importPeopleStep(): Step = stepBuilderFactory.get("importPeopleStep")
            .chunk<Person, Person>(1)
            .reader(importPeopleReader("IMPORT_FILE_PATH"))
            .processor(upperCasePeopleProcessor())
            .writer(importPeopleWriter())
            .build()

    // importPeopleReader
    @Bean
    @StepScope
    open fun importPeopleReader(
            @Value("#{jobParameters[importFilePath]}") importFilePath: String
    ): FlatFileItemReader<Person> = FlatFileItemReader<Person>().apply {
        setResource(FileSystemResource(importFilePath))
        setLineMapper(importPeopleLineMapper())
    }

    @Bean
    open fun importPeopleLineMapper(): DefaultLineMapper<Person> = DefaultLineMapper<Person>().apply {
        setLineTokenizer(importPeopleLineTokenizer())
        setFieldSetMapper(importPeopleFieldSetMapper())
    }

    @Bean
    open fun importPeopleLineTokenizer(): DelimitedLineTokenizer = DelimitedLineTokenizer().apply {
        setDelimiter(",")
        setNames(arrayOf("firstName", "lastName"))
    }

    @Bean
    open fun importPeopleFieldSetMapper(): BeanWrapperFieldSetMapper<Person> = BeanWrapperFieldSetMapper<Person>().apply {
        setTargetType(Person::class.java)
    }

    // importPeopleWriter
    @Bean
    open fun importPeopleWriter(): JdbcBatchItemWriter<Person> = JdbcBatchItemWriter<Person>().apply {
        setDataSource(dataSource)
        setItemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider<Person>())
        setSql("INSERT INTO family.person(first_name, last_name) VALUES (:firstName, :lastName)")
    }

    // upperCasePeopleProcessor
    @Bean
    open fun upperCasePeopleProcessor(): ItemProcessor<Person, Person> = UpperCasePeopleProcessor()
}