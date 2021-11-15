package app.web.v1.people

import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import app.web.v1.msteams.TeamsConfigDTO
import io.swagger.annotations.{Api, ApiOperation, ApiParam, ApiResponse, ApiResponses}

import javax.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.{CrossOrigin, PathVariable, RequestBody, RequestHeader, RequestMapping, RequestMethod, ResponseStatus, RestController}
import springfox.documentation.annotations.ApiIgnore

import scala.util.Try

@CrossOrigin
@RestController
@RequestMapping(Array("/v1"))
@Api(description = "manage people", tags = Array("people"))
class PeopleController(peopleDao: PeopleDao, settingsDao: SettingsDao, peopleBusiness: PeopleBusiness) {

  private val LOG = LoggerFactory.getLogger(this.getClass)

  @ApiOperation(
    value = "Returns all people in their rotation order",
    produces = "application/json",
    code = 200)
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, message = "Bad request")
  ))
  @RequestMapping(value = Array("/people"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def listPeople(@ApiIgnore @RequestHeader("user-id") userId: String): ResponseEntity[Any] = {

    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId
    val pointerNumber = settingsDao.settings(teamId).get.orderPointer

    val people = peopleDao
      .loadAllPeopleOrdered(teamId)
      .map(person => PersonDTO.fromPerson(person, pointerNumber == person.order))

    new ResponseEntity(people, HttpStatus.OK)
  }

  @ApiOperation(
    value = "Update the people on your team",
    produces = "application/json",
    code = 200)
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Bad Request"),
  ))
  @RequestMapping(value = Array("/people"), method = Array(RequestMethod.PUT))
  @Transactional
  def updatePeople(@ApiIgnore @RequestHeader("user-id") userId: String,
                   @ApiParam(value = "people") @RequestBody peopleUpdate: Array[PersonDTO]): ResponseEntity[Any] = {
    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId
    val pointerNumber = settingsDao.settings(teamId).get.orderPointer
    val personOnSupport = peopleDao.loadPersonByOrder(teamId, pointerNumber).get

    val userIdsUnique = peopleUpdate.map(_.userId).distinct.length == peopleUpdate.length
    if (!userIdsUnique)
      return new ResponseEntity(ErrorMessageDTO("All user IDs must be unique"), HttpStatus.BAD_REQUEST)

    if (peopleUpdate.length < 2)
      return new ResponseEntity(ErrorMessageDTO("There must be at least two people for a team"), HttpStatus.BAD_REQUEST)

    if (peopleUpdate.length > 15)
      return new ResponseEntity(ErrorMessageDTO("There cannot be more than six people on a team"), HttpStatus.BAD_REQUEST)

    val updatesHavePersonOnSupport = peopleUpdate.find(person => personOnSupport.userId == person.userId).isDefined
    if (!updatesHavePersonOnSupport)
      return new ResponseEntity(ErrorMessageDTO(s"The person on support cannot be removed from the team"), HttpStatus.BAD_REQUEST)

    //Delete all team members and create the new ones
    peopleDao.loadAllPeopleOrdered(teamId).map(person => peopleDao.removePerson(teamId, person.id))
    peopleUpdate.map(person => peopleDao.insertPerson(person.name, person.userId, teamId))

    //Update the pointer to point to the person who is on support - may be in a different position in the order
    val personOnSupportNew = peopleDao.loadPersonByUserId(personOnSupport.userId).get
    settingsDao.setPointer(teamId, personOnSupportNew.order)

    new ResponseEntity(HttpStatus.OK)
  }

}
