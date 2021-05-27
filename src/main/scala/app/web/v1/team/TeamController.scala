package app.web.v1.team

import app.dao.{PeopleDao, TeamDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation._
import springfox.documentation.annotations.ApiIgnore

@CrossOrigin
@RestController
@RequestMapping(Array("/v1"))
@Api(description = "manage your team", tags = Array("team"))
class TeamController(peopleDao: PeopleDao, teamDao: TeamDao) {

  private val Log: Logger = LoggerFactory.getLogger(this.getClass)

  @ApiOperation(
    value = "Get your team ID",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK")
  ))
  @RequestMapping(value = Array("/team/id"), method = Array(RequestMethod.GET))
  @Transactional
  def getMyTeamId(@ApiIgnore @RequestHeader("user-id") userId: String): ResponseEntity[TeamIdDTO] = {
    val teamId : Option[Int] = peopleDao.loadPersonByUserId(userId).map(_.teamId)

    new ResponseEntity(TeamIdDTO(teamId, teamId.isDefined), HttpStatus.OK)
  }

  @ApiOperation(
    value = "Get your team name",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, response = classOf[ErrorMessageDTO], message = "Bad Request")
  ))
  @RequestMapping(value = Array("/team/name"), method = Array(RequestMethod.GET))
  @Transactional
  def getMyTeamName(@ApiIgnore @RequestHeader("user-id") userId: String): ResponseEntity[Any] = {
    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    val team = teamDao.getTeam(teamId)
    new ResponseEntity(team.get.name, HttpStatus.OK)
  }

  @ApiOperation(
    value = "Update your team name",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK")
  ))
  @RequestMapping(value = Array("/team/name/{teamName}"), method = Array(RequestMethod.PUT))
  @Transactional
  def setTeamName(@ApiIgnore @RequestHeader("user-id") userId: String,
                  @PathVariable(name = "teamName", required = true) teamName: String): ResponseEntity[Any] = {
    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    teamDao.updateTeamName(teamId, teamName)

    new ResponseEntity(HttpStatus.OK)
  }

  @ApiOperation(
    value = "Delete your team",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK")
  ))
  @RequestMapping(value = Array("/team"), method = Array(RequestMethod.DELETE))
  @Transactional
  def deleteTeam(@ApiIgnore @RequestHeader("user-id") userId: String): ResponseEntity[Any] = {
    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    teamDao.deleteTeam(teamId)

    new ResponseEntity(HttpStatus.OK)
  }

}
