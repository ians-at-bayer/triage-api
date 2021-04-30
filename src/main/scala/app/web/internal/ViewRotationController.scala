package app.web.internal

import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import app.web.v1.people.RotationConfigDTO
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation._
import springfox.documentation.annotations.ApiIgnore

import scala.util.Try

@CrossOrigin
@RestController
@RequestMapping(Array("/webapp/"))
@ApiIgnore
class ViewRotationController(peopleDao: PeopleDao, settingsDao: SettingsDao) {

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
}
