package app.web.v1.people

import app.business.{MessageGenerator, SlackNotifier}
import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation._
import springfox.documentation.annotations.ApiIgnore

import scala.util.Try

@CrossOrigin
@RestController
@RequestMapping(Array("/v1"))
@Api(description = "manage who is on call", tags = Array("on-call"))
class OnCallController(peopleDao: PeopleDao,
                       settingsDao: SettingsDao,
                       messageGenerator: MessageGenerator,
                       slackNotifier: SlackNotifier) {

  @ApiOperation(
    value = "Set who is on call currently. Will notify Slack with the change.",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Bad Request"),
    new ApiResponse(code = 404, response = classOf[ErrorMessageDTO], message = "Not Found")
  ))
  @RequestMapping(value = Array("/on-call/{personId}"), method = Array(RequestMethod.PUT))
  @Transactional
  def setOnCallPerson(@ApiIgnore @RequestHeader("user-id") userId: String,
                       @PathVariable(name = "personId", required = true) personIdString: String): ResponseEntity[Any]  = {

    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    val personId = Try(personIdString.toInt) recover {
      case e: NumberFormatException => return new ResponseEntity(ErrorMessageDTO("Person ID format is invalid"), HttpStatus.BAD_REQUEST)
    }

    val person = peopleDao.loadPerson(teamId, personId.get)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"Person not found"), HttpStatus.NOT_FOUND))

    settingsDao.setPointer(person.teamId, person.order)

    slackNotifier.sendMessage(person.teamId, messageGenerator.generateMessage(person))

    new ResponseEntity(HttpStatus.OK)
  }

  @ApiOperation(
    value = "See who is on call currently",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Bad Request")
  ))
  @RequestMapping(value = Array("/on-call"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def getOnCallPerson(@ApiIgnore @RequestHeader("user-id") userId: String): ResponseEntity[Any]  = {

    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    val team = settingsDao.settings(teamId)

    if (team.isEmpty) return new ResponseEntity(ErrorMessageDTO("Team ID not found"), HttpStatus.BAD_REQUEST)

    val personOnCall = peopleDao.loadPersonByOrder(team.get.teamId, team.get.orderPointer)

    new ResponseEntity(PersonDTO.fromPerson(personOnCall.get, true), HttpStatus.OK)
  }

}
