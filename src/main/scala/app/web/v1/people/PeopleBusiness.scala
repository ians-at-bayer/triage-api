package app.web.v1.people

import app.dao.PeopleDao
import org.springframework.stereotype.Component

@Component
class PeopleBusiness(peopleDao: PeopleDao) {
  def createPeople(teamId: Int, peopleToCreate: Seq[PersonDTO]): Unit = {
    if (!peopleToCreate.forall(_.rotationOrderNumber.isEmpty))
      throw new IllegalArgumentException("Rotation order cannot be set when adding a person")

    if (!peopleToCreate.forall(_.onSupport.isEmpty))
      throw new IllegalArgumentException("On support flag cannot be set when adding a person")

    peopleToCreate.foreach(person => {
      if (peopleDao.loadPersonByUserId(person.slackId).isDefined)
        throw new IllegalArgumentException(s"Person '${person.slackId}' already exists")

      peopleDao.insertPerson(person.name, person.slackId, teamId)
    })
  }
}
