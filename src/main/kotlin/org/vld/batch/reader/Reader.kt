package org.vld.batch.reader

import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.UnexpectedInputException
import org.springframework.classify.Classifier
import org.vld.batch.builder.AggregateItemBuilder

enum class ReadStrategy {
    FAIL_ON_ERROR,
    CONTINUE_ON_ERROR
}

class AggregateItemReader<I, O>(
        private val itemReader: ItemReader<I>,
        private val builderClassifier: Classifier<I, AggregateItemBuilder<O>>,
        private val readStrategy: ReadStrategy = ReadStrategy.FAIL_ON_ERROR
) : ItemReader<O> {

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

class SplitItemReader<I, O>(
    private val itemReader: ItemReader<I>,
    private val iteratorClassifier: Classifier<I, Iterator<O>>
): ItemReader<O> {

    private var itemIterator: Iterator<O>? = null

    override fun read(): O? {
        if (itemIterator == null) {
            val aggregateItem = itemReader.read()
            if (aggregateItem == null) return aggregateItem
            itemIterator = iteratorClassifier.classify(aggregateItem)
        }
        return if (itemIterator?.hasNext() ?: false) {
            itemIterator?.next()
        } else {
            itemIterator = null
            read()
        }
    }
}
