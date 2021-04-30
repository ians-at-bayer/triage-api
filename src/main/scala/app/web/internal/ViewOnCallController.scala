package app.web.internal

import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import app.web.v1.people.PersonDTO
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation._
import springfox.documentation.annotations.ApiIgnore

import scala.util.Try

@CrossOrigin
@RestController
@RequestMapping(Array("/webapp/"))
@ApiIgnore
class ViewOnCallController(peopleDao: PeopleDao,
                           settingsDao: SettingsDao) {

  @RequestMapping(value = Array("/on-call/{teamId}"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def getOnCallPerson(@PathVariable(name = "teamId", required = true) teamIdString: String): ResponseEntity[Any]  = {

    val teamIdOption = Try(teamIdString.toInt) recover {
      case e: NumberFormatException => return new ResponseEntity(ErrorMessageDTO("Team ID format is invalid"), HttpStatus.BAD_REQUEST)
    }

    val teamId = teamIdOption.get
    val team = settingsDao.settings(teamId)

    if (team.isEmpty) return new ResponseEntity(ErrorMessageDTO("Team ID not found"), HttpStatus.BAD_REQUEST)

    val personOnCall = peopleDao.loadPersonByOrder(team.get.teamId, team.get.orderPointer)

    new ResponseEntity(PersonDTO.fromPerson(personOnCall.get, true), HttpStatus.OK)
  }

}
