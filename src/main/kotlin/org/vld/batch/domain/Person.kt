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
    fun <L> add(line: L): MultilineItemBuilder<T>
    fun build(): T
}

class MaleBuilder : MultilineItemBuilder<Male> {

    private var maleName: MaleName = MaleName()
    private var maleContact: MaleContact = MaleContact()

    override fun <MaleLine> add(line: MaleLine): MultilineItemBuilder<Male> = this.apply {
        when (line) {
            is MaleName -> maleName = line
            is MaleContact -> maleContact = line
        }
    }

    override fun build(): Male = Male(maleName.firstName, maleName.lastName, maleContact.email, maleContact.phone)
}

class FemaleBuilder : MultilineItemBuilder<Female> {
    
    private var femaleName: FemaleName = FemaleName()
    private var femaleContact: FemaleContact = FemaleContact()

    override fun <FemaleLine> add(line: FemaleLine): MultilineItemBuilder<Female> = this.apply {
        when (line) {
            is FemaleName -> femaleName = line
            is FemaleContact -> femaleContact = line
        }
    }

    override fun build(): Female = Female(femaleName.firstName, femaleName.lastName, femaleContact.email, femaleContact.phone)
}