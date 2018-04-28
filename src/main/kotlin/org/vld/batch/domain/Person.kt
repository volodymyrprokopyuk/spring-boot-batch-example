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
