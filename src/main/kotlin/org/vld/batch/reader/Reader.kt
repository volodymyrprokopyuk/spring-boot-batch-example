package org.vld.batch.reader

import org.springframework.batch.item.ItemReader
import org.vld.batch.domain.FemaleBuilder
import org.vld.batch.domain.FemaleEnd
import org.vld.batch.domain.FemaleLine
import org.vld.batch.domain.HumanLine
import org.vld.batch.domain.MaleBuilder
import org.vld.batch.domain.MaleEnd
import org.vld.batch.domain.MaleLine

class AggregateItemReader<T>(private val itemReader: ItemReader<HumanLine>) : ItemReader<T> {

    @Suppress("UNCHECKED_CAST")
    override fun read(): T? {
        var line: HumanLine? = itemReader.read()
        when (line) {
            is MaleLine -> {
                val itemBuilder = MaleBuilder()
                while (line !is MaleEnd) {
                    itemBuilder.add(line)
                    line = itemReader.read()
                }
                return itemBuilder.build() as T
            }
            is FemaleLine -> {
                val itemBuilder = FemaleBuilder()
                while (line !is FemaleEnd) {
                    itemBuilder.add(line)
                    line = itemReader.read()
                }
                return itemBuilder.build() as T
            }
            else -> return null
        }
    }
}