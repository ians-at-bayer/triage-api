package app.web.v1.slack

import app.business.{MessageGenerator, SlackNotifier}
import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations.{Api, ApiOperation, ApiParam, ApiResponse, ApiResponses}

import javax.servlet.http.HttpServletRequest
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.{CrossOrigin, PathVariable, RequestBody, RequestMapping, RequestMethod, ResponseStatus, RestController}

import scala.util.Try

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
  @RequestMapping(value = Array("/slack-config/{teamId}"), method = Array(RequestMethod.POST))
  @Transactional
  def setConfig(@PathVariable(name = "teamId", required = true) teamIdString: String,
                @ApiParam(value = "slackConfig") @RequestBody slackConfig: SlackConfigDTO
               ): ResponseEntity[Any] = {

    val teamIdOption = Try(teamIdString.toInt) recover {
      case e: NumberFormatException => return new ResponseEntity(ErrorMessageDTO("Team ID format is invalid"), HttpStatus.BAD_REQUEST)
    }

    val team = settingsDao.settings(teamIdOption.get)
    if (team.isEmpty) return new ResponseEntity(ErrorMessageDTO("Team ID not found"), HttpStatus.BAD_REQUEST)

    val teamId = team.get.teamId

    if (slackConfig.slackHookUrl.isDefined) settingsDao.setSlackHookURL(teamId, slackConfig.slackHookUrl.get)

    if(slackConfig.slackMessage.isDefined) settingsDao.setSlackMessage(teamId, slackConfig.slackMessage.get)

    new ResponseEntity(HttpStatus.OK)
  }

  @ApiOperation(
    value = "See the slack configuration",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK")
  ))
  @RequestMapping(value = Array("/slack-config/{teamId}"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def viewConfig(@PathVariable(name = "teamId", required = true) teamIdString: String): ResponseEntity[Any]  = {
    val teamIdOption = Try(teamIdString.toInt) recover {
      case e: NumberFormatException => return new ResponseEntity(ErrorMessageDTO("Team ID format is invalid"), HttpStatus.BAD_REQUEST)
    }

    val team = settingsDao.settings(teamIdOption.get)
    if (team.isEmpty) return new ResponseEntity(ErrorMessageDTO("Team ID not found"), HttpStatus.BAD_REQUEST)

    val teamId = team.get.teamId

    val settings = settingsDao.settings(teamId).get

    new ResponseEntity(SlackConfigDTO(Some(settings.slackHookUrl), Some(settings.slackMessage)), HttpStatus.OK)
  }

  @ApiOperation(
    value = "Send or resend slack messages",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK")
  ))
  @RequestMapping(value = Array("/slack-send/{teamId}"), method = Array(RequestMethod.GET))
  def resendSlackNotification(@PathVariable(name = "teamId", required = true) teamIdString: String): ResponseEntity[Any] = {
    val teamIdOption = Try(teamIdString.toInt) recover {
      case e: NumberFormatException => return new ResponseEntity(ErrorMessageDTO("Team ID format is invalid"), HttpStatus.BAD_REQUEST)
    }

    val team = settingsDao.settings(teamIdOption.get)
    if (team.isEmpty) return new ResponseEntity(ErrorMessageDTO("Team ID not found"), HttpStatus.BAD_REQUEST)

    val teamId = team.get.teamId

    val personOnCall = peopleDao.loadPersonByOrder(teamId, settingsDao.settings(teamId).get.orderPointer)
    slackNotifier.sendMessage(teamId, messageGenerator.generateMessage(personOnCall.get))

    new ResponseEntity(HttpStatus.OK)
  }

}
