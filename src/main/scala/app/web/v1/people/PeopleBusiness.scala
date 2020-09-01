package app.web.v1.people

import app.dao.PeopleDao
import org.springframework.stereotype.Component

@Component
class PeopleBusiness(peopleDao: PeopleDao) {
  def createPeople(peopleToCreate: Seq[PersonDTO]): Unit = {
    if (!peopleToCreate.forall(_.id.isEmpty))
      throw new IllegalArgumentException("Person ID cannot be set when adding a person")

    if (!peopleToCreate.forall(_.rotationOrderNumber.isEmpty))
      throw new IllegalArgumentException("Rotation order cannot be set when adding a person")

    if (!peopleToCreate.forall(_.onSupport.isEmpty))
      throw new IllegalArgumentException("On support flag cannot be set when adding a person")

    if (peopleToCreate.length < 2)
      throw new IllegalArgumentException("There must be at least two people to rotate")

    peopleToCreate.foreach(person => peopleDao.insertPerson(person.name, person.slackId))
  }
}
