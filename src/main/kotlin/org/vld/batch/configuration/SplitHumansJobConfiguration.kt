package org.vld.batch.configuration

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper
import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper
import org.springframework.batch.item.file.transform.LineTokenizer
import org.springframework.batch.item.file.transform.PassThroughLineAggregator
import org.springframework.batch.item.file.transform.RegexLineTokenizer
import org.springframework.batch.item.support.ClassifierCompositeItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.vld.batch.domain.Female
import org.vld.batch.domain.FemaleBegin
import org.vld.batch.domain.FemaleContact
import org.vld.batch.domain.FemaleEnd
import org.vld.batch.domain.FemaleName
import org.vld.batch.domain.Human
import org.vld.batch.builder.HumanItemBuilderClassifier
import org.vld.batch.domain.HumanLine
import org.vld.batch.domain.Male
import org.vld.batch.domain.MaleBegin
import org.vld.batch.domain.MaleContact
import org.vld.batch.domain.MaleEnd
import org.vld.batch.domain.MaleName
import org.vld.batch.reader.AggregateItemReader

@Configuration
@EnableBatchProcessing
open class SplitHumansJobConfiguration {

    @Autowired
    private lateinit var jobBuilderFactory: JobBuilderFactory

    @Autowired
    private lateinit var stepBuilderFactory: StepBuilderFactory

    // ** splitHumansJob
    @Bean
    open fun splitHumansJob(): Job = jobBuilderFactory.get("splitHumansJob")
            .incrementer(RunIdIncrementer())
            .start(splitHumansStep())
            .build()

    // splitHumansStep
    @Bean
    open fun splitHumansStep(): Step = stepBuilderFactory.get("splitHumansStep")
            .chunk<Human, Human>(1)
            .reader(aggregateHumansReader())
            .writer(splitHumansWriter())
            .stream(splitHumansReader("IMPORT_HUMANS_FILE_PATH"))
            .stream(splitMalesWriter("EXPORT_MALES_FILE_PATH"))
            .stream(splitFemalesWriter("EXPORT_FEMALES_FILE_PATH"))
            .build()

    // splitHumansReader
    @Bean
    @StepScope
    open fun splitHumansReader(
            @Value("#{jobParameters[importHumansFilePath]}") importHumansFilePath: String
    ): FlatFileItemReader<HumanLine> = FlatFileItemReader<HumanLine>().apply {
        setResource(FileSystemResource(importHumansFilePath))
        setLineMapper(splitHumansLineMapper())
    }

    @Bean
    open fun splitHumansLineMapper(): PatternMatchingCompositeLineMapper<HumanLine> = PatternMatchingCompositeLineMapper<HumanLine>().apply {
        setTokenizers(splitHumansLineTokenizers())
        setFieldSetMappers(splitHumansFieldSetMappers())
    }

    @Bean
    open fun splitHumansLineTokenizers(): Map<String, LineTokenizer> = mutableMapOf<String, LineTokenizer>().apply {
        this["MALE BEGIN*"] = RegexLineTokenizer().apply {
            setRegex("""(MALE BEGIN)""")
            setNames(arrayOf("label"))
        }
        this["MALE NAME*"] = RegexLineTokenizer().apply {
            setRegex("""(MALE NAME):([^,]*),(.*)""")
            setNames(arrayOf("label", "firstName", "lastName"))
        }
        this["MALE CONTACT*"] = RegexLineTokenizer().apply {
            setRegex("""(MALE CONTACT):([^,]*),(.*)""")
            setNames(arrayOf("label", "email", "phone"))
        }
        this["MALE END*"] = RegexLineTokenizer().apply {
            setRegex("""(MALE END)""")
            setNames(arrayOf("label"))
        }

        this["FEMALE BEGIN*"] = RegexLineTokenizer().apply {
            setRegex("""(FEMALE BEGIN)""")
            setNames(arrayOf("label"))
        }
        this["FEMALE NAME*"] = RegexLineTokenizer().apply {
            setRegex("""(FEMALE NAME):([^,]*),(.*)""")
            setNames(arrayOf("label", "firstName", "lastName"))
        }
        this["FEMALE CONTACT*"] = RegexLineTokenizer().apply {
            setRegex("""(FEMALE CONTACT):([^,]*),(.*)""")
            setNames(arrayOf("label", "email", "phone"))
        }
        this["FEMALE END*"] = RegexLineTokenizer().apply {
            setRegex("""(FEMALE END)""")
            setNames(arrayOf("label"))
        }
    }

    @Bean
    @Suppress("UNCHECKED_CAST")
    open fun splitHumansFieldSetMappers(): Map<String, FieldSetMapper<HumanLine>?>
            = mutableMapOf<String, FieldSetMapper<HumanLine>?>().apply {
        this["MALE BEGIN*"] = BeanWrapperFieldSetMapper<MaleBegin>().apply { setTargetType(MaleBegin::class.java) }
                as FieldSetMapper<HumanLine>
        this["MALE NAME*"] = BeanWrapperFieldSetMapper<MaleName>().apply { setTargetType(MaleName::class.java) }
                as FieldSetMapper<HumanLine>
        this["MALE CONTACT*"] = BeanWrapperFieldSetMapper<MaleContact>().apply { setTargetType(MaleContact::class.java) }
                as FieldSetMapper<HumanLine>
        this["MALE END*"] = BeanWrapperFieldSetMapper<MaleEnd>().apply { setTargetType(MaleEnd::class.java) }
                as FieldSetMapper<HumanLine>

        this["FEMALE BEGIN*"] = BeanWrapperFieldSetMapper<FemaleBegin>().apply { setTargetType(FemaleBegin::class.java) }
                as FieldSetMapper<HumanLine>
        this["FEMALE NAME*"] = BeanWrapperFieldSetMapper<FemaleName>().apply { setTargetType(FemaleName::class.java) }
                as FieldSetMapper<HumanLine>
        this["FEMALE CONTACT*"] = BeanWrapperFieldSetMapper<FemaleContact>().apply { setTargetType(FemaleContact::class.java) }
                as FieldSetMapper<HumanLine>
        this["FEMALE END*"] = BeanWrapperFieldSetMapper<FemaleEnd>().apply { setTargetType(FemaleEnd::class.java) }
                as FieldSetMapper<HumanLine>
    }

    // aggregateHumansReader
    @Bean
    open fun aggregateHumansReader(): AggregateItemReader<HumanLine, Human> = AggregateItemReader(
            splitHumansReader("MULTI_HUMANS_FILE_PATH"),
            HumanItemBuilderClassifier()/*,
            ReadStrategy.CONTINUE_ON_ERROR*/
    )

    // splitHumansWriter
    @Bean
    open fun splitHumansWriter(): ClassifierCompositeItemWriter<Human> = ClassifierCompositeItemWriter<Human>().apply {
        setClassifier { classifiable ->
            @Suppress("UNCHECKED_CAST")
            when (classifiable) {
                is Male -> splitMalesWriter("EXPORT_MALES_FILE_PATH") as ItemWriter<Human>
                is Female -> splitFemalesWriter("EXPORT_FEMALES_FILE_PATH") as ItemWriter<Human>
                else -> throw IllegalArgumentException("Unknown classifieble $classifiable")
            }
        }
    }

    @Bean
    @StepScope
    open fun splitMalesWriter(
            @Value("#{jobParameters[exportMalesFilePath]}") exportMalesFilePath: String
    ): FlatFileItemWriter<Male> = FlatFileItemWriter<Male>().apply {
        setResource(FileSystemResource(exportMalesFilePath))
        setLineAggregator(PassThroughLineAggregator<Male>())
    }

    @Bean
    @StepScope
    open fun splitFemalesWriter(
            @Value("#{jobParameters[exportFemalesFilePath]}") exportFemalesFilePath: String
    ): FlatFileItemWriter<Female> = FlatFileItemWriter<Female>().apply {
        setResource(FileSystemResource(exportFemalesFilePath))
        setLineAggregator(PassThroughLineAggregator<Female>())
    }
}