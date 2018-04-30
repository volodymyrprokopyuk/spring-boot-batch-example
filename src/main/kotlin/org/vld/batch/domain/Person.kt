package org.vld.batch.domain

import org.springframework.classify.Classifier

data class Person(
        var firstName: String = "",
        var lastName: String = ""
)

interface Human {
    val errors: List<String>
}

data class Male(
        var firstName: String = "",
        var lastName: String = "",
        var email: String = "",
        var phone: String = "",
        override val errors: List<String> = listOf()
) : Human

data class Female(
        var firstName: String = "",
        var lastName: String = "",
        var email: String = "",
        var phone: String = "",
        override val errors: List<String> = listOf()
) : Human

interface HumanLine {
    var label: String
}

interface MaleLine : HumanLine
interface FemaleLine : HumanLine

data class MaleBegin(override var label: String = "") : MaleLine
data class MaleName(override var label: String = "", var firstName: String = "", var lastName: String = "") : MaleLine
data class MaleContact(override var label: String = "", var email: String = "", var phone: String = "") : MaleLine
data class MaleEnd(override var label: String = "") : MaleLine

data class FemaleBegin(override var label: String = "") : FemaleLine
data class FemaleName(override var label: String = "", var firstName: String = "", var lastName: String = "") : FemaleLine
data class FemaleContact(override var label: String = "", var email: String = "", var phone: String = "") : FemaleLine
data class FemaleEnd(override var label: String = "") : FemaleLine

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
) : AggregateItemBuilder<Male> {

    private var maleBegin: MaleBegin? = null
    private var maleName: MaleName? = null
    private var maleContact: MaleContact? = null
    private var maleEnd: MaleEnd? = null
    private var expectedLabels = listOf("MALE BEGIN")

    override fun <MaleLine> add(line: MaleLine): AggregateItemBuilder<Male> = this.apply {
        when (line) {
            is MaleBegin -> {
                maleBegin = line
                validate(line)
                expectedLabels = listOf("MALE NAME")
            }
            is MaleName -> {
                maleName = line
                validate(line)
                expectedLabels = listOf("MALE CONTACT")
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

    override fun build(): Male = Male(
            maleName?.firstName ?: "",
            maleName?.lastName ?: "",
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
) : AggregateItemBuilder<Female> {

    private var femaleBegin: FemaleBegin? = null
    private var femaleName: FemaleName? = null
    private var femaleContact: FemaleContact? = null
    private var femaleEnd: FemaleEnd? = null
    private var expectedLabels = listOf("FEMALE BEGIN")

    override fun <FemaleLine> add(line: FemaleLine): AggregateItemBuilder<Female> = this.apply {
        when (line) {
            is FemaleBegin -> {
                femaleBegin = line
                validate(line)
                expectedLabels = listOf("FEMALE NAME")
            }
            is FemaleName -> {
                femaleName = line
                validate(line)
                expectedLabels = listOf("FEMALE CONTACT")
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

    override fun build(): Female = Female(
            femaleName?.firstName ?: "",
            femaleName?.lastName ?: "",
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

    @Suppress("UNCHECKED_CAST")
    override fun classify(line: HumanLine): AggregateItemBuilder<Human> = when (line) {
        is MaleLine -> MaleBuilder() as AggregateItemBuilder<Human>
        is FemaleLine -> FemaleBuilder() as AggregateItemBuilder<Human>
        else -> throw IllegalArgumentException("Unknown line $line")
    }
}