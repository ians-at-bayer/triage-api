package app.web.v1.slack

import app.business.{MessageGenerator, SlackNotifier}
import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations.{Api, ApiOperation, ApiParam, ApiResponse, ApiResponses}
import javax.servlet.http.HttpServletRequest
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.{CrossOrigin, RequestBody, RequestMapping, RequestMethod, ResponseStatus, RestController}

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
  @RequestMapping(value = Array("/slack-config"), method = Array(RequestMethod.POST))
  @Transactional
  def setConfig(httpRequest: HttpServletRequest,
                @ApiParam(value = "slackConfig") @RequestBody slackConfig: SlackConfigDTO
               ): ResponseEntity[Any] = {

    if (slackConfig.slackHookUrl.isDefined)
      settingsDao.setSlackHookURL(slackConfig.slackHookUrl.get)

    if(slackConfig.slackMessage.isDefined)
      settingsDao.setSlackMessage(slackConfig.slackMessage.get)

    new ResponseEntity(HttpStatus.OK)
  }

  @ApiOperation(
    value = "See the slack configuration",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK")
  ))
  @RequestMapping(value = Array("/slack-config"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def viewConfig(): SlackConfigDTO  = {
    val settings = settingsDao.settings

    SlackConfigDTO(Some(settings.slackHookUrl), Some(settings.slackMessage))
  }

  @ApiOperation(
    value = "Resend slack messages",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK")
  ))
  @RequestMapping(value = Array("/slack-resend"), method = Array(RequestMethod.GET))
  def resendSlackNotification(): ResponseEntity[Any] = {
    val personOnCall = peopleDao.loadPersonByOrder(settingsDao.settings.orderPointer)

    slackNotifier.sendMessage(messageGenerator.generateMessage(personOnCall.get))

    new ResponseEntity(HttpStatus.OK)
  }

}
