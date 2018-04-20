package org.vld.batch.processor

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.item.ItemProcessor
import org.vld.batch.domain.Person

class UpperCasePeopleProcessor : ItemProcessor<Person, Person> {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(UpperCasePeopleProcessor::class.java)
    }

    override fun process(person: Person?): Person {
        val newPerson = Person(
                firstName = person?.firstName ?: "Unknown",
                lastName = person?.lastName ?: "Unknown"
        )
        if (newPerson.lastName.contains("fail")) throw IllegalArgumentException("Person.lastName contains `fail`")
        val upperCasePerson = Person(
                firstName = newPerson.firstName.toUpperCase(),
                lastName = newPerson.lastName.toUpperCase()
        )
        logger.info("PROCESSED: $upperCasePerson")
        return upperCasePerson
    }
}