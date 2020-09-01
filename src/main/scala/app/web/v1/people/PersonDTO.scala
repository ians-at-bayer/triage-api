package app.web.v1.people

import app.dao.Person

case class PersonDTO(
                      id: Option[Int] = None,
                      name: String,
                      slackId: String,
                      rotationOrderNumber: Option[Int] = None,
                      onSupport : Option[Boolean] = None)

object PersonDTO {
  def fromPerson(person: Person, onSupport: Boolean): PersonDTO =
    PersonDTO(Some(person.id), person.name, person.slackId, Some(person.order), Some(onSupport))
}