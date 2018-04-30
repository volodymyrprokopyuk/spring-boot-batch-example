package org.vld.batch.reader

import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.UnexpectedInputException
import org.springframework.classify.Classifier
import org.vld.batch.domain.AggregateItemBuilder

enum class ReadStrategy {
    FAIL_ON_ERROR,
    CONTINUE_ON_ERROR
}

class AggregateItemReader<I, O>(
        private val itemReader: ItemReader<I>,
        private val builderClassifier: Classifier<I, AggregateItemBuilder<O>>,
        private val readStrategy: ReadStrategy = ReadStrategy.FAIL_ON_ERROR
) : ItemReader<O> {

    @Suppress("UNCHECKED_CAST")
    override fun read(): O? {
        var line = itemReader.read()
        if (line == null) return line
        val itemBuilder = builderClassifier.classify(line)
        itemBuilder.add(line)
        failOnErrorIfRequested(itemBuilder)
        while (!itemBuilder.isComplete) {
            line = itemReader.read()
            itemBuilder.add(line)
            failOnErrorIfRequested(itemBuilder)
        }
        return itemBuilder.build()
    }

    private fun <O> failOnErrorIfRequested(itemBuilder: AggregateItemBuilder<O>) {
        if (readStrategy == ReadStrategy.FAIL_ON_ERROR && !itemBuilder.isValid) {
            throw UnexpectedInputException("${itemBuilder.errors}")
        }
    }
}