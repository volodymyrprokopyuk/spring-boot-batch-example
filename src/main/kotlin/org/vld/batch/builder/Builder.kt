package org.vld.batch.builder

import org.springframework.classify.Classifier
import org.vld.batch.domain.AggregateFemale
import org.vld.batch.domain.AggregateMale
import org.vld.batch.domain.Female
import org.vld.batch.domain.FemaleBegin
import org.vld.batch.domain.FemaleContact
import org.vld.batch.domain.FemaleEnd
import org.vld.batch.domain.FemaleLine
import org.vld.batch.domain.FemaleName
import org.vld.batch.domain.Human
import org.vld.batch.domain.HumanLine
import org.vld.batch.domain.Male
import org.vld.batch.domain.MaleBegin
import org.vld.batch.domain.MaleContact
import org.vld.batch.domain.MaleEnd
import org.vld.batch.domain.MaleLine
import org.vld.batch.domain.MaleName

interface AggregateItemBuilder<T> {
    var isValid: Boolean
    var isComplete: Boolean
    val errors: MutableList<String>
    fun <L> add(line: L): AggregateItemBuilder<T>
    fun build(): T
}

class MaleBuilder(
        override var isValid: Boolean = true,
        override var isComplete: Boolean = false,
        override val errors: MutableList<String> = mutableListOf()
) : AggregateItemBuilder<AggregateMale> {

    private var maleBegin: MaleBegin? = null
    private var maleNames: MutableList<MaleName> = mutableListOf()
    private var maleContact: MaleContact? = null
    private var maleEnd: MaleEnd? = null
    private var expectedLabels = listOf("MALE BEGIN")

    override fun <MaleLine> add(line: MaleLine): AggregateItemBuilder<AggregateMale> = this.apply {
        when (line) {
            is MaleBegin -> {
                maleBegin = line
                validate(line)
                expectedLabels = listOf("MALE NAME")
            }
            is MaleName -> {
                maleNames.add(line)
                validate(line)
                expectedLabels = listOf("MALE NAME", "MALE CONTACT")
            }
            is MaleContact -> {
                maleContact = line
                validate(line)
                expectedLabels = listOf("MALE END")
            }
            is MaleEnd -> {
                maleEnd = line
                validate(line)
                expectedLabels = listOf("MALE BEGIN")
                isComplete = true
            }
        }
    }

    override fun build(): AggregateMale = AggregateMale(
            maleNames,
            maleContact?.email ?: "",
            maleContact?.phone ?: "",
            errors
    )

    private fun validate(line: HumanLine) {
        if (!expectedLabels.contains(line.label)) {
            errors.add("Expected $expectedLabels but ${line.label} found")
            isComplete = true
            isValid = false
        }
    }
}

class FemaleBuilder(
        override var isValid: Boolean = true,
        override var isComplete: Boolean = false,
        override val errors: MutableList<String> = mutableListOf()
) : AggregateItemBuilder<AggregateFemale> {

    private var femaleBegin: FemaleBegin? = null
    private var femaleNames: MutableList<FemaleName> = mutableListOf()
    private var femaleContact: FemaleContact? = null
    private var femaleEnd: FemaleEnd? = null
    private var expectedLabels = listOf("FEMALE BEGIN")

    override fun <FemaleLine> add(line: FemaleLine): AggregateItemBuilder<AggregateFemale> = this.apply {
        when (line) {
            is FemaleBegin -> {
                femaleBegin = line
                validate(line)
                expectedLabels = listOf("FEMALE NAME")
            }
            is FemaleName -> {
                femaleNames.add(line)
                validate(line)
                expectedLabels = listOf("FEMALE NAME", "FEMALE CONTACT")
            }
            is FemaleContact -> {
                femaleContact = line
                validate(line)
                expectedLabels = listOf("FEMALE END")
            }
            is FemaleEnd -> {
                femaleEnd = line
                validate(line)
                expectedLabels = listOf("FEMALE BEGIN")
                isComplete = true
            }
        }
    }

    override fun build(): AggregateFemale = AggregateFemale(
            femaleNames,
            femaleContact?.email ?: "",
            femaleContact?.phone ?: "",
            errors
    )

    private fun validate(line: HumanLine) {
        if (!expectedLabels.contains(line.label)) {
            errors.add("Expected $expectedLabels but ${line.label} found")
            isComplete = true
            isValid = false
        }
    }
}

class HumanItemBuilderClassifier : Classifier<HumanLine, AggregateItemBuilder<Human>> {

    override fun classify(line: HumanLine): AggregateItemBuilder<Human> =
            @Suppress("UNCHECKED_CAST")
            when (line) {
                is MaleLine -> MaleBuilder() as AggregateItemBuilder<Human>
                is FemaleLine -> FemaleBuilder() as AggregateItemBuilder<Human>
                else -> throw IllegalArgumentException("Unknown line $line")
            }
}

class MaleIterator(private val male: AggregateMale) : Iterator<Male> {

    private var index = 0

    override fun hasNext(): Boolean = index < male.names.size

    override fun next(): Male {
        if (!hasNext()) throw ArrayIndexOutOfBoundsException("AggregateMale.names is exhausted")
        val maleName = male.names[index++]
        return Male(maleName.firstName, maleName.lastName, male.email, male.phone, male.errors)
    }
}

class FemaleIterator(private val female: AggregateFemale) : Iterator<Female> {

    private var index = 0

    override fun hasNext(): Boolean = index < female.names.size

    override fun next(): Female {
        if (!hasNext()) throw ArrayIndexOutOfBoundsException("AggregateFemale.names is exhausted")
        val femaleName = female.names[index++]
        return Female(femaleName.firstName, femaleName.lastName, female.email, female.phone, female.errors)
    }
}

class HumanItemIteratorClassifier : Classifier<Human, Iterator<Human>> {

    override fun classify(human: Human): Iterator<Human> = when (human) {
        is AggregateMale -> MaleIterator(human)
        is AggregateFemale -> FemaleIterator(human)
        else -> throw IllegalArgumentException("Unknown human $human")
    }
}