package app.web.v1.people

import app.dao.PeopleDao
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
@Api(description = "manage the rotation order", tags = Array("rotation-order"))
class RotationOrderController(peopleDao: PeopleDao) {

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
