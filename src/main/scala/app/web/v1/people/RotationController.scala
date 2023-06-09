package app.web.v1.people

import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations.{Api, ApiOperation, ApiParam, ApiResponse, ApiResponses}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.{CrossOrigin, RequestBody, RequestHeader, RequestMapping, RequestMethod, RestController}
import springfox.documentation.annotations.ApiIgnore

import java.time.Instant
import java.time.temporal.ChronoUnit

@CrossOrigin
@RestController
@RequestMapping(Array("/v1"))
@Api(description = "manage rotations", tags = Array("rotations"))
class RotationController(peopleDao: PeopleDao, settingsDao: SettingsDao) {

  @ApiOperation(
    value = "Update the rotation config",
    produces = "application/json",
    code = 200)
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Bad Request"),
  ))
  @RequestMapping(value = Array("/rotation-config"), method = Array(RequestMethod.PUT))
  @Transactional
  def setConfig(@ApiIgnore @RequestHeader("user-id") userId: String,
                @ApiParam(value = "config") @RequestBody config: RotationConfigDTO
               ): ResponseEntity[Any] = {

    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    if (config.nextRotationTime.isDefined) {
      val rotationTime = config.nextRotationTime.get

      val diff = Instant.now().until(rotationTime, ChronoUnit.NANOS)
      if (diff <= 0) return new ResponseEntity(ErrorMessageDTO("The next rotation time must be later than the current time"), HttpStatus.BAD_REQUEST)

      settingsDao.setNextRotation(teamId, rotationTime)
    }

    if (config.rotationFrequencyDays.isDefined)
      settingsDao.setRotationFrequencyDays(teamId, config.rotationFrequencyDays.get)

    new ResponseEntity(HttpStatus.OK)
  }

  @ApiOperation(
    value = "See the rotation config",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Bad Request")
  ))
  @RequestMapping(value = Array("/rotation-config"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def viewConfig(@ApiIgnore @RequestHeader("user-id") userId: String): ResponseEntity[Any]  = {
    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId
    val settings = settingsDao.settings(teamId).get

    new ResponseEntity(RotationConfigDTO(Some(settings.nextRotation), Some(settings.rotationFrequency)), HttpStatus.OK)
  }

}
