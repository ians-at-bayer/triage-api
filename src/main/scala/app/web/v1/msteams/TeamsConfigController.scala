package app.web.v1.msteams

import app.business.{MessageGenerator, Notifier}
import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations._
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation._
import springfox.documentation.annotations.ApiIgnore

import scala.util.Try

@CrossOrigin
@RestController
@RequestMapping(Array("/v1"))
@Api(description = "manage the microsoft teams integration", tags = Array("microsoft-teams-integration"))
class TeamsConfigController(settingsDao: SettingsDao,
                            notifier: Notifier,
                            messageGenerator: MessageGenerator,
                            peopleDao: PeopleDao) {

  private val Log: Logger = LoggerFactory.getLogger(this.getClass)

  @ApiOperation(
    value = "Update the teams configuration",
    produces = "application/json",
    code = 200)
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Bad Request"),
  ))
  @RequestMapping(value = Array("/teams-config"), method = Array(RequestMethod.PUT))
  @Transactional
  def setConfig(@ApiIgnore @RequestHeader("user-id") userId: String,
                @ApiParam(value = "teamsConfig") @RequestBody teamsConfig: TeamsConfigDTO
               ): ResponseEntity[Any] = {

    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    if (teamsConfig.hookUrl.isDefined) settingsDao.setHookURL(teamId, teamsConfig.hookUrl.get)

    if(teamsConfig.message.isDefined) settingsDao.setMessage(teamId, teamsConfig.message.get)

    new ResponseEntity(HttpStatus.OK)
  }

  @ApiOperation(
    value = "See the teams configuration",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, message = "User has no team assigned")
  ))
  @RequestMapping(value = Array("/teams-config"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def viewConfig(@ApiIgnore @RequestHeader("user-id") userId: String): ResponseEntity[Any]  = {
    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    val settings = settingsDao.settings(teamId).get

    new ResponseEntity(TeamsConfigDTO(Some(settings.hookUrl), Some(settings.hookMessage)), HttpStatus.OK)
  }

  @ApiOperation(
    value = "Send or resend teams messages",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, message = "User has no team assigned")
  ))
  @RequestMapping(value = Array("/teams-send"), method = Array(RequestMethod.GET))
  def resendTeamsNotification(@ApiIgnore @RequestHeader("user-id") userId: String): ResponseEntity[Any] = {
    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    val personOnCall = peopleDao.loadPersonByOrder(teamId, settingsDao.settings(teamId).get.orderPointer).get
    val teamsSendResult = Try(notifier.sendMessage(teamId, messageGenerator.generateMessage(personOnCall)))
    if (teamsSendResult.isFailure || !teamsSendResult.get) {
      if (teamsSendResult.isFailure) Log.error("Failed to send teams message", teamsSendResult.failed.get)

      return new ResponseEntity(ErrorMessageDTO(s"Failed to send a Teams message. Please check your settings and try again later."), HttpStatus.BAD_REQUEST)
    }

    new ResponseEntity(HttpStatus.OK)
  }

}
