package org.vld.batch.domain

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