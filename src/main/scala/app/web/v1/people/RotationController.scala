package app.web.v1.people

import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations.{Api, ApiOperation, ApiParam, ApiResponse, ApiResponses}

import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.{CrossOrigin, PathVariable, RequestBody, RequestMapping, RequestMethod, ResponseStatus, RestController}

import java.time.Instant
import scala.util.Try
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
  @RequestMapping(value = Array("/rotation-config/{teamId}"), method = Array(RequestMethod.POST))
  @Transactional
  def setConfig(@PathVariable(name = "teamId", required = true) teamIdString: String,
                @ApiParam(value = "config") @RequestBody config: RotationConfigDTO
               ): ResponseEntity[Any] = {

    val teamIdOption = Try(teamIdString.toInt) recover {
      case e: NumberFormatException => return new ResponseEntity(ErrorMessageDTO("Team ID format is invalid"), HttpStatus.BAD_REQUEST)
    }

    val team = settingsDao.settings(teamIdOption.get)
    if (team.isEmpty) return new ResponseEntity(ErrorMessageDTO("Team ID not found"), HttpStatus.BAD_REQUEST)

    val teamId = team.get.teamId

    if (config.nextRotationTime.isDefined) {
      val rotationTime = config.nextRotationTime.get

      val diff = Instant.now().until(rotationTime, ChronoUnit.NANOS)
      if (diff <= 0) return new ResponseEntity("The next rotation time must be later than the current time", HttpStatus.BAD_REQUEST)

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
    new ApiResponse(code = 200, message = "OK")
  ))
  @RequestMapping(value = Array("/rotation-config/{teamId}"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def viewConfig(@PathVariable(name = "teamId", required = true) teamIdString: String): ResponseEntity[Any]  = {
    val teamIdOption = Try(teamIdString.toInt) recover {
      case e: NumberFormatException => return new ResponseEntity(ErrorMessageDTO("Team ID format is invalid"), HttpStatus.BAD_REQUEST)
    }

    val team = settingsDao.settings(teamIdOption.get)
    if (team.isEmpty) return new ResponseEntity(ErrorMessageDTO("Team ID not found"), HttpStatus.BAD_REQUEST)

    val teamId = team.get.teamId
    val settings = settingsDao.settings(teamId).get

    new ResponseEntity(RotationConfigDTO(Some(settings.nextRotation), Some(settings.rotationFrequency)), HttpStatus.OK)
  }

  @ApiOperation(
    value = "Change the rotation order",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 204, message = "No Content"),
    new ApiResponse(code = 404, response = classOf[ErrorMessageDTO], message = "Not Found")
  ))
  @ResponseStatus(value = HttpStatus.NO_CONTENT) // This annotation necessary to keep swagger from listing 200 as a possible response code.
  @RequestMapping(value = Array("/rotation-order/{teamId}"), method = Array(RequestMethod.POST), consumes = Array("application/json"))
  @Transactional
  def reorderPeople(@PathVariable(name = "teamId", required = true) teamIdString: String,
                    @ApiParam(value = "rotationOrderByPersonId") @RequestBody request: Array[Int]): ResponseEntity[Any]  = {
    val teamIdOption = Try(teamIdString.toInt) recover {
      case e: NumberFormatException => return new ResponseEntity(ErrorMessageDTO("Team ID format is invalid"), HttpStatus.BAD_REQUEST)
    }

    val team = settingsDao.settings(teamIdOption.get)
    if (team.isEmpty) return new ResponseEntity(ErrorMessageDTO("Team ID not found"), HttpStatus.BAD_REQUEST)

    val teamId = team.get.teamId

    Try (peopleDao.changeOrder(teamId, request)) recover {
      case e: IllegalArgumentException => return new ResponseEntity(ErrorMessageDTO(e.getMessage), HttpStatus.NOT_FOUND)
    }

    new ResponseEntity(HttpStatus.NO_CONTENT)
  }



}
