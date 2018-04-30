package org.vld.batch.reader

import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.UnexpectedInputException
import org.springframework.classify.Classifier
import org.vld.batch.domain.HumanLine
import org.vld.batch.domain.MultilineItemBuilder

enum class ReadStrategy {
    FAIL_ON_ERROR,
    CONTINUE_ON_ERROR
}

class AggregateItemReader<T>(
        private val itemReader: ItemReader<HumanLine>,
        private val builderClassifier: Classifier<HumanLine, MultilineItemBuilder<T>>,
        private val readStrategy: ReadStrategy = ReadStrategy.FAIL_ON_ERROR
) : ItemReader<T> {

    @Suppress("UNCHECKED_CAST")
    override fun read(): T? {
        var line = itemReader.read()
        if (line == null) return line
        val itemBuilder = builderClassifier.classify(line)
        itemBuilder.add(line)
        failOnErrorIfRequired(itemBuilder)
        while (!itemBuilder.isComplete) {
            line = itemReader.read()
            itemBuilder.add(line)
            failOnErrorIfRequired(itemBuilder)
        }
        return itemBuilder.build()
    }

    private fun <T> failOnErrorIfRequired(itemBuilder: MultilineItemBuilder<T>) {
        if (readStrategy == ReadStrategy.FAIL_ON_ERROR && !itemBuilder.isValid) {
            throw UnexpectedInputException("${itemBuilder.errors}")
        }
    }
}