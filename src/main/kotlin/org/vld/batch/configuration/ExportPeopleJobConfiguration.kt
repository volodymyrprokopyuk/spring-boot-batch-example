package org.vld.batch.configuration

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor
import org.springframework.batch.item.file.transform.DelimitedLineAggregator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.vld.batch.domain.Person
import org.vld.batch.processor.UpperCasePeopleProcessor
import javax.sql.DataSource

@Configuration
@EnableBatchProcessing
open class ExportPeopleJobConfiguration {

    @Autowired
    private lateinit var dataSource: DataSource

    @Autowired
    private lateinit var jobBuilderFactory: JobBuilderFactory

    @Autowired
    private lateinit var stepBuilderFactory: StepBuilderFactory

    // ** exportPeopleJob
    @Bean
    open fun exportPeopleJob(): Job = jobBuilderFactory.get("exportPeopleJob")
            .incrementer(RunIdIncrementer())
            .start(exportPeopleStep())
            .build()

    // exportPeopleStep
    @Bean
    open fun exportPeopleStep(): Step = stepBuilderFactory.get("exportPeopleStep")
            .chunk<Person, Person>(1)
            .reader(exportPeopleReader())
            .processor(upperCasePeopleProcessor())
            .writer(exportPeopleWriter("EXPORT_FILE_PATH"))
            .build()

    // exportPeopleReader
    @Bean
    open fun exportPeopleReader(): JdbcCursorItemReader<Person> {
        val reader = JdbcCursorItemReader<Person>()
        reader.dataSource = dataSource
        reader.sql = "SELECT p.first_name, p.last_name FROM family.person p"
        reader.setRowMapper(BeanPropertyRowMapper(Person::class.java))
        return reader
    }

    // exportPeopleWriter
    @Bean
    @StepScope
    open fun exportPeopleWriter(
            @Value("#{jobParameters[exportFilePath]}") exportFilePath: String
    ): FlatFileItemWriter<Person> = FlatFileItemWriter<Person>().apply {
        setResource(FileSystemResource(exportFilePath))
        setLineAggregator(exportPeopleLineAggregator())
    }

    @Bean
    open fun exportPeopleLineAggregator(): DelimitedLineAggregator<Person> = DelimitedLineAggregator<Person>().apply {
        setDelimiter(",")
        setFieldExtractor(exportPeopleFieldExtractor())
    }

    @Bean
    open fun exportPeopleFieldExtractor(): BeanWrapperFieldExtractor<Person> = BeanWrapperFieldExtractor<Person>().apply {
        setNames(arrayOf("firstName", "lastName"))
    }

    // upperCasePeopleProcessor
    @Bean
    open fun upperCasePeopleProcessor(): ItemProcessor<Person, Person> = UpperCasePeopleProcessor()
}