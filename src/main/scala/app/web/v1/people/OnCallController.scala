package app.web.v1.people

import app.business.{MessageGenerator, SlackNotifier}
import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation._

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
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Invalid format"),
    new ApiResponse(code = 404, response = classOf[ErrorMessageDTO], message = "Not Found")
  ))
  @RequestMapping(value = Array("/on-call/{personId}"), method = Array(RequestMethod.PUT))
  @Transactional
  def setOnCallPerson(@PathVariable(name = "personId", required = true) personIdString: String): ResponseEntity[Any]  = {

    val personId = Try(personIdString.toInt) recover {
      case e: NumberFormatException => return new ResponseEntity(ErrorMessageDTO("Person ID format is invalid"), HttpStatus.BAD_REQUEST)
    }

    val person = peopleDao.loadPerson(personId.get)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"Person not found"), HttpStatus.NOT_FOUND))

    settingsDao.setPointer(person.order)

    slackNotifier.sendMessage(messageGenerator.generateMessage(person))

    new ResponseEntity(HttpStatus.OK)
  }

  @ApiOperation(
    value = "See who is on call currently",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK")
  ))
  @RequestMapping(value = Array("/on-call"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def getOnCallPerson(): PersonDTO  = {
    val personOnCall = peopleDao.loadPersonByOrder(settingsDao.settings.orderPointer)

    PersonDTO.fromPerson(personOnCall.get, true)
  }

}
