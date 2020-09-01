package app.web.v1.people

import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations.{Api, ApiOperation, ApiParam, ApiResponse, ApiResponses}
import javax.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.{CrossOrigin, RequestBody, RequestMapping, RequestMethod, ResponseStatus, RestController}

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
  @RequestMapping(value = Array("/people"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def listPeople(): ResponseEntity[Any] = {
    val pointerNumber = settingsDao.settings.orderPointer

    val people = peopleDao
      .loadAllPeopleOrdered()
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
  @RequestMapping(value = Array("/people"), method = Array(RequestMethod.POST), produces = Array("application/json; charset=utf-8"))
  @Transactional
  def addPeople(@ApiParam(value = "peopleToAdd") @RequestBody people: Array[PersonDTO]): ResponseEntity[Any] = {

    Try(peopleBusiness.createPeople(people)) recover {
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
  @RequestMapping(value = Array("/people"), method = Array(RequestMethod.DELETE), consumes = Array("application/json"))
  @Transactional
  def deletePeople(httpRequest: HttpServletRequest,
                   @ApiParam(value = "peopleIdsToDelete") @RequestBody request: Array[Int]): ResponseEntity[Any]  = {

    val onCallPersonId = peopleDao.loadPersonByOrder(settingsDao.settings.orderPointer).get.id

    if (request.contains(onCallPersonId))
      return new ResponseEntity(ErrorMessageDTO("Cannot delete the person who is currently on call"), HttpStatus.BAD_REQUEST)

    request.foreach(personId => {
      Try (peopleDao.removePerson(personId)) recover {
        case e: IllegalArgumentException => return new ResponseEntity(e.getMessage, HttpStatus.NOT_FOUND)
      }
    })

    new ResponseEntity(HttpStatus.NO_CONTENT)
  }

}
