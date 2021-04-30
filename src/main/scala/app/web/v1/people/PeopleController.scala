package app.web.v1.people

import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations.{Api, ApiOperation, ApiParam, ApiResponse, ApiResponses}

import javax.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.{CrossOrigin, PathVariable, RequestBody, RequestMapping, RequestMethod, ResponseStatus, RestController}

import scala.util.Try

@CrossOrigin
@RestController
@RequestMapping(Array("/v1"))
@Api(description = "manage people", tags = Array("people"))
class PeopleController(peopleDao: PeopleDao, settingsDao: SettingsDao, peopleBusiness: PeopleBusiness) {

  private val LOG = LoggerFactory.getLogger(this.getClass)

  @ApiOperation(
    value = "Returns all people in their rotation order",
    produces = "application/json",
    code = 200)
  @RequestMapping(value = Array("/people/{teamId}"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def listPeople(@PathVariable(name = "teamId", required = true) teamIdString: String): ResponseEntity[Any] = {

    val teamIdOption = Try(teamIdString.toInt) recover {
      case e: NumberFormatException => return new ResponseEntity(ErrorMessageDTO("Team ID format is invalid"), HttpStatus.BAD_REQUEST)
    }

    val team = settingsDao.settings(teamIdOption.get)
    if (team.isEmpty) return new ResponseEntity(ErrorMessageDTO("Team ID not found"), HttpStatus.BAD_REQUEST)

    val teamId = team.get.teamId
    val pointerNumber = settingsDao.settings(teamId).get.orderPointer

    val people = peopleDao
      .loadAllPeopleOrdered(teamId)
      .map(person => PersonDTO.fromPerson(person, pointerNumber == person.order))

    new ResponseEntity(people, HttpStatus.OK)
  }

  @ApiOperation(
    value = "Add one or more people",
    produces = "application/json",
    code = 201)
  @ApiResponses(value = Array(
    new ApiResponse(code = 201, message = "Created"),
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Bad Request"),
  ))
  @ResponseStatus(value = HttpStatus.CREATED) // This annotation necessary to keep swagger from listing 200 as a possible response code.
  @RequestMapping(value = Array("/people/{teamId}"), method = Array(RequestMethod.POST), produces = Array("application/json; charset=utf-8"))
  @Transactional
  def addPeople(@PathVariable(name = "teamId", required = true) teamIdString: String,
                 @ApiParam(value = "peopleToAdd") @RequestBody people: Array[PersonDTO]): ResponseEntity[Any] = {

    val teamIdOption = Try(teamIdString.toInt) recover {
      case e: NumberFormatException => return new ResponseEntity(ErrorMessageDTO("Team ID format is invalid"), HttpStatus.BAD_REQUEST)
    }

    val team = settingsDao.settings(teamIdOption.get)
    if (team.isEmpty) return new ResponseEntity(ErrorMessageDTO("Team ID not found"), HttpStatus.BAD_REQUEST)

    val teamId = team.get.teamId

    Try(peopleBusiness.createPeople(teamId, people)) recover {
      case e: IllegalArgumentException => return new ResponseEntity(ErrorMessageDTO(e.getMessage), HttpStatus.BAD_REQUEST)
    }

    new ResponseEntity(HttpStatus.CREATED)
  }

  @ApiOperation(
    value = "Delete one or more people",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 204, message = "No Content"),
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Bad Request"),
    new ApiResponse(code = 404, response = classOf[ErrorMessageDTO], message = "Not Found")
  ))
  @ResponseStatus(value = HttpStatus.NO_CONTENT) // This annotation necessary to keep swagger from listing 200 as a possible response code.
  @RequestMapping(value = Array("/people/{teamId}"), method = Array(RequestMethod.DELETE), consumes = Array("application/json"))
  @Transactional
  def deletePeople(@PathVariable(name = "teamId", required = true) teamIdString: String,
                   @ApiParam(value = "peopleIdsToDelete") @RequestBody request: Array[Int]): ResponseEntity[Any]  = {

    val teamIdOption = Try(teamIdString.toInt) recover {
      case e: NumberFormatException => return new ResponseEntity(ErrorMessageDTO("Team ID format is invalid"), HttpStatus.BAD_REQUEST)
    }

    val team = settingsDao.settings(teamIdOption.get)
    if (team.isEmpty) return new ResponseEntity(ErrorMessageDTO("Team ID not found"), HttpStatus.BAD_REQUEST)

    val teamId = team.get.teamId

    val onCallPersonId = peopleDao.loadPersonByOrder(teamId, settingsDao.settings(teamId).get.orderPointer).get.id

    if (request.contains(onCallPersonId))
      return new ResponseEntity(ErrorMessageDTO("Cannot delete the person who is currently on call"), HttpStatus.BAD_REQUEST)

    request.foreach(personId => {
      Try (peopleDao.removePerson(teamId, personId)) recover {
        case e: IllegalArgumentException => return new ResponseEntity(e.getMessage, HttpStatus.NOT_FOUND)
      }
    })

    new ResponseEntity(HttpStatus.NO_CONTENT)
  }

}
