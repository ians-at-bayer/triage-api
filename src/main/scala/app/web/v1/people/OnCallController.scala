package app.web.v1.people

import app.business.{MessageGenerator, Notifier}
import app.dao.{PeopleDao, SettingsDao, TeamDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionAspectSupport
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
                       notifier: Notifier,
                       teamDao: TeamDao) {

  private val Log: Logger = LoggerFactory.getLogger(this.getClass)

  @ApiOperation(
    value = "Set who is on call currently. Will notify teams with the change.",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Bad Request"),
    new ApiResponse(code = 404, response = classOf[ErrorMessageDTO], message = "Not Found")
  ))
  @RequestMapping(value = Array("/on-call/{userId}"), method = Array(RequestMethod.PUT))
  @Transactional
  def setOnCallPerson(@ApiIgnore @RequestHeader("user-id") actionUserId: String,
                      @PathVariable(name = "userId", required = true) onCallUserId: String): ResponseEntity[Any]  = {

    val teamId = peopleDao.loadPersonByUserId(actionUserId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${actionUserId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    val person = peopleDao.loadAllPeopleOrdered(teamId).find(_.userId == onCallUserId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"Person not found"), HttpStatus.NOT_FOUND))

    settingsDao.setPointer(person.teamId, person.order)

    val teamsSendResult = Try(notifier.sendMessage(teamId, messageGenerator.generateMessage(person)))
    if (teamsSendResult.isFailure || !teamsSendResult.get) {
      if (teamsSendResult.isFailure) Log.error("Failed to send teams message", teamsSendResult.failed.get)

      TransactionAspectSupport.currentTransactionStatus.setRollbackOnly()
      return new ResponseEntity(ErrorMessageDTO(s"Failed to send a Teams message. Please check your settings and try again later."), HttpStatus.BAD_REQUEST)
    }

    new ResponseEntity(HttpStatus.OK)
  }

  @ApiOperation(
    value = "See who is on call for your team currently",
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

  @ApiOperation(
    value = "See who is on call currently for any team",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Bad Request")
  ))
  @RequestMapping(value = Array("/on-call/{teamId}"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def getOnCallPersonForTeam(@PathVariable(name = "teamId", required = true) teamIdString: String): ResponseEntity[Any]  = {
    val teamId : Int = Try (teamIdString.toInt)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO("Team ID is not value"), HttpStatus.BAD_REQUEST))

    val teamSettings = settingsDao.settings(teamId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO("Team ID not found"), HttpStatus.BAD_REQUEST))
    val team = teamDao.getTeam(teamId).get

    val personOnCall = peopleDao.loadPersonByOrder(teamSettings.teamId, teamSettings.orderPointer).get

    new ResponseEntity(TeamMemberOnCall(personOnCall.name, team.name, personOnCall.userId, teamSettings.nextRotation), HttpStatus.OK)
  }

}
