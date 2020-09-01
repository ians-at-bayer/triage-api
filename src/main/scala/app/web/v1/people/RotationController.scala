package app.web.v1.people

import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations.{Api, ApiOperation, ApiParam, ApiResponse, ApiResponses}
import javax.servlet.http.HttpServletRequest
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.{CrossOrigin, RequestBody, RequestMapping, RequestMethod, ResponseStatus, RestController}

import scala.util.Try

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
  @RequestMapping(value = Array("/rotation-config"), method = Array(RequestMethod.POST))
  @Transactional
  def setConfig(@ApiParam(value = "config") @RequestBody config: RotationSettingsDTO
               ): ResponseEntity[Any] = {

    if (config.nextRotationTime.isDefined)
      settingsDao.setNextRotation(config.nextRotationTime.get)

    if (config.rotationFrequencyDays.isDefined)
      settingsDao.setRotationFrequencyDays(config.rotationFrequencyDays.get)

    new ResponseEntity(HttpStatus.OK)
  }

  @ApiOperation(
    value = "See the rotation config",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK")
  ))
  @RequestMapping(value = Array("/rotation-config"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def viewConfig(): RotationSettingsDTO  = {
    val settings = settingsDao.settings

    new RotationSettingsDTO(Some(settings.nextRotation), Some(settings.rotationFrequency))
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
  @RequestMapping(value = Array("/rotation-order"), method = Array(RequestMethod.PATCH), consumes = Array("application/json"))
  @Transactional
  def reorderPeople(httpRequest: HttpServletRequest,
                   @ApiParam(value = "rotationOrderByPersonId") @RequestBody request: Array[Int]): ResponseEntity[Any]  = {

    Try (peopleDao.changeOrder(request)) recover {
      case e: IllegalArgumentException => return new ResponseEntity(e.getMessage, HttpStatus.NOT_FOUND)
    }

    new ResponseEntity(HttpStatus.NO_CONTENT)
  }



}
