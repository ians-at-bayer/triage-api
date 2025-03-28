package app.web.v1.chatbot

import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations._
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation._
import springfox.documentation.annotations.ApiIgnore

@CrossOrigin
@RestController
@RequestMapping(Array("/v1"))
@Api(description = "manage the mygenassist chatbot integration", tags = Array("mygenassist-integration"))
class ChatbotConfigController(settingsDao: SettingsDao, peopleDao: PeopleDao) {

  private val Log: Logger = LoggerFactory.getLogger(this.getClass)

  @ApiOperation(
    value = "Update the mygenassist configuration",
    produces = "application/json",
    code = 200)
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Bad Request"),
  ))
  @RequestMapping(value = Array("/chatbot-config"), method = Array(RequestMethod.PUT))
  @Transactional
  def setConfig(@ApiIgnore @RequestHeader("user-id") userId: String,
                @ApiParam(value = "chatbotConfig") @RequestBody chatbotConfig: ChatbotDTO
               ): ResponseEntity[Any] = {

    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    settingsDao.setChatbotId(teamId, chatbotConfig.chatbotId)

    new ResponseEntity(HttpStatus.OK)
  }

  @ApiOperation(
    value = "See the chatbot configuration",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, message = "User has no team assigned")
  ))
  @RequestMapping(value = Array("/chatbot-config"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def viewConfig(@ApiIgnore @RequestHeader("user-id") userId: String): ResponseEntity[Any]  = {
    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    val settings = settingsDao.settings(teamId).get

    new ResponseEntity(ChatbotDTO(settings.chatbotId), HttpStatus.OK)
  }


}
