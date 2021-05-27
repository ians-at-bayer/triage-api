package app.web.v1.slack

import app.business.{MessageGenerator, SlackNotifier}
import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations._
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation._
import springfox.documentation.annotations.ApiIgnore

@CrossOrigin
@RestController
@RequestMapping(Array("/v1"))
@Api(description = "manage the slack integration", tags = Array("slack-integration"))
class SlackConfigController(settingsDao: SettingsDao,
                            slackNotifier: SlackNotifier,
                            messageGenerator: MessageGenerator,
                            peopleDao: PeopleDao) {

  @ApiOperation(
    value = "Update the slack configuration",
    produces = "application/json",
    code = 200)
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Bad Request"),
  ))
  @RequestMapping(value = Array("/slack-config"), method = Array(RequestMethod.PUT))
  @Transactional
  def setConfig(@ApiIgnore @RequestHeader("user-id") userId: String,
                @ApiParam(value = "slackConfig") @RequestBody slackConfig: SlackConfigDTO
               ): ResponseEntity[Any] = {

    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    if (slackConfig.slackHookUrl.isDefined) settingsDao.setSlackHookURL(teamId, slackConfig.slackHookUrl.get)

    if(slackConfig.slackMessage.isDefined) settingsDao.setSlackMessage(teamId, slackConfig.slackMessage.get)

    new ResponseEntity(HttpStatus.OK)
  }

  @ApiOperation(
    value = "See the slack configuration",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, message = "User has no team assigned")
  ))
  @RequestMapping(value = Array("/slack-config"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def viewConfig(@ApiIgnore @RequestHeader("user-id") userId: String): ResponseEntity[Any]  = {
    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    val settings = settingsDao.settings(teamId).get

    new ResponseEntity(SlackConfigDTO(Some(settings.slackHookUrl), Some(settings.slackMessage)), HttpStatus.OK)
  }

  @ApiOperation(
    value = "Send or resend slack messages",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, message = "User has no team assigned")
  ))
  @RequestMapping(value = Array("/slack-send"), method = Array(RequestMethod.GET))
  def resendSlackNotification(@ApiIgnore @RequestHeader("user-id") userId: String): ResponseEntity[Any] = {
    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    val personOnCall = peopleDao.loadPersonByOrder(teamId, settingsDao.settings(teamId).get.orderPointer)
    slackNotifier.sendMessage(teamId, messageGenerator.generateMessage(personOnCall.get))

    new ResponseEntity(HttpStatus.OK)
  }

}
