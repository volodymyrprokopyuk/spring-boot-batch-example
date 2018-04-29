package org.vld.batch.reader

import org.springframework.batch.item.ItemReader
import org.vld.batch.domain.Human
import org.vld.batch.domain.HumanLine
import org.vld.batch.domain.MultilineItemBuilder
import org.vld.batch.domain.MultilineItemBuilderResolver

class AggregateItemReader<T>(
        private val itemReader: ItemReader<HumanLine>,
        private val builderResolver: MultilineItemBuilderResolver<HumanLine, MultilineItemBuilder<Human>>
) : ItemReader<T> {

    @Suppress("UNCHECKED_CAST")
    override fun read(): T? {
        var line = itemReader.read()
        if (line == null) return line
        val itemBuilder = builderResolver.resolve(line)
        itemBuilder.add(line)
        while (!itemBuilder.isComplete) {
            line = itemReader.read()
            itemBuilder.add(line)
        }
        return itemBuilder.build() as T
    }
}