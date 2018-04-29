package org.vld.batch.domain

data class Person(var firstName: String = "", var lastName: String = "")

interface Human

data class Male(
        var firstName: String = "", var lastName: String = "", var email: String = "", var phone: String = ""
) : Human

data class Female(
        var firstName: String = "", var lastName: String = "", var email: String = "", var phone: String = ""
) : Human

interface HumanLine
interface MaleLine : HumanLine
interface FemaleLine : HumanLine

data class MaleBegin(var label: String = "") : MaleLine
data class MaleName(var firstName: String = "", var lastName: String = "") : MaleLine
data class MaleContact(var email: String = "", var phone: String = "") : MaleLine
data class MaleEnd(var label: String = "") : MaleLine

data class FemaleBegin(var label: String = "") : FemaleLine
data class FemaleName(var firstName: String = "", var lastName: String = "") : FemaleLine
data class FemaleContact(var email: String = "", var phone: String = "") : FemaleLine
data class FemaleEnd(var label: String = "") : FemaleLine

interface MultilineItemBuilder<T> {
    var isValid: Boolean
    var isComplete: Boolean
    fun <L> add(line: L): MultilineItemBuilder<T>
    fun build(): T
}

class MaleBuilder(
        override var isValid: Boolean = false,
        override var isComplete: Boolean = false
) : MultilineItemBuilder<Male> {

    private var maleBegin: MaleBegin? = null
    private var maleName: MaleName? = null
    private var maleContact: MaleContact? = null
    private var maleEnd: MaleEnd? = null

    override fun <MaleLine> add(line: MaleLine): MultilineItemBuilder<Male> = this.apply {
        when (line) {
            is MaleBegin -> maleBegin = line
            is MaleName -> maleName = line
            is MaleContact -> maleContact = line
            is MaleEnd -> {
                maleEnd = line
                isComplete = true
            }
        }
    }

    override fun build(): Male = Male(
            maleName?.firstName ?: "",
            maleName?.lastName ?: "",
            maleContact?.email ?: "",
            maleContact?.phone ?: ""
    )
}

class FemaleBuilder(
        override var isValid: Boolean = false,
        override var isComplete: Boolean = false
) : MultilineItemBuilder<Female> {

    private var femaleBegin: FemaleBegin? = null
    private var femaleName: FemaleName? = null
    private var femaleContact: FemaleContact? = null
    private var femaleEnd: FemaleEnd? = null

    override fun <FemaleLine> add(line: FemaleLine): MultilineItemBuilder<Female> = this.apply {
        when (line) {
            is FemaleBegin -> femaleBegin = line
            is FemaleName -> femaleName = line
            is FemaleContact -> femaleContact = line
            is FemaleEnd -> {
                femaleEnd = line
                isComplete = true
            }
        }
    }

    override fun build(): Female = Female(
            femaleName?.firstName ?: "",
            femaleName?.lastName ?: "",
            femaleContact?.email ?: "",
            femaleContact?.phone ?: ""
    )
}

interface MultilineItemBuilderResolver<L, B> {
    fun resolve(line: L): B
}

class HumanBuilderResolver : MultilineItemBuilderResolver<HumanLine, MultilineItemBuilder<Human>> {

    @Suppress("UNCHECKED_CAST")
    override fun resolve(line: HumanLine): MultilineItemBuilder<Human> = when (line) {
        is MaleLine -> MaleBuilder() as MultilineItemBuilder<Human>
        is FemaleLine -> FemaleBuilder() as MultilineItemBuilder<Human>
        else -> throw IllegalArgumentException("Unknown line $line")
    }
}