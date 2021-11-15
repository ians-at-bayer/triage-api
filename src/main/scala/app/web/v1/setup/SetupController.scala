package app.web.v1.setup

import app.business.{MessageGenerator, Notifier}
import app.dao.{PeopleDao, Settings, SettingsDao, TeamDao}
import app.web.v1.ErrorMessageDTO
import io.swagger.annotations._
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation._
import springfox.documentation.annotations.ApiIgnore

import java.time.temporal.ChronoUnit
import java.time.Instant
import scala.util.Try
import org.springframework.transaction.interceptor.TransactionAspectSupport

@CrossOrigin
@RestController
@RequestMapping(Array("/v1"))
@Api(description = "setup your team for the first time ", tags = Array("setup"))
class SetupController(settingsDao: SettingsDao,
                      peopleDao: PeopleDao,
                      teamDao: TeamDao,
                      notifier: Notifier,
                      messageGenerator: MessageGenerator) {

  private val Log: Logger = LoggerFactory.getLogger(this.getClass)

  @ApiOperation(
    value = "Submit the first time setup for your team",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 201, message = "Team Created")))
  @ResponseStatus(value = HttpStatus.CREATED) // This annotation necessary to keep swagger from listing 200 as a possible response code.
  @RequestMapping(value = Array("/setup"), method = Array(RequestMethod.POST), consumes = Array("application/json"))
  @Transactional
  def setup(@ApiIgnore @RequestHeader("user-id") userId: String,
            @ApiParam(value = "setup") @RequestBody setup: SetupDTO): ResponseEntity[Any]  = {

    //Validate
    if (setup.people.length < 2)
      return new ResponseEntity(ErrorMessageDTO("There must be at least two people for rotations to occur"), HttpStatus.BAD_REQUEST)

    if (setup.people.find(p => p.userId.toLowerCase == userId.toLowerCase).isEmpty)
      return new ResponseEntity(ErrorMessageDTO("You must be on the team you are creating"), HttpStatus.BAD_REQUEST)

    val diff = Instant.now().until(setup.rotationConfig.nextRotationTime, ChronoUnit.NANOS)
    if (diff <= 0)
      return new ResponseEntity(ErrorMessageDTO("The next rotation time must be later than the current time"), HttpStatus.BAD_REQUEST)

    //Create the team
    val teamId = teamDao.createTeam(setup.teamName)

    Log.info(s"User ${userId} is attempting to create team ${setup.teamName} [assigned team id:$teamId]")

    //Save people
    setup.people.foreach(people => {
      if (peopleDao.loadPersonByUserId(people.userId).isDefined) {
        TransactionAspectSupport.currentTransactionStatus.setRollbackOnly() //rollback because we may have made db changes by now
        return new ResponseEntity(ErrorMessageDTO(s"User ${people.name} is already on a team"), HttpStatus.BAD_REQUEST)
      }

      peopleDao.insertPerson(people.name, people.userId, teamId)
    })

    //Save settings
    settingsDao.create(Settings(setup.teamsConfig.hookUrl, teamId, 0,setup.teamsConfig.message,
      setup.rotationConfig.nextRotationTime, setup.rotationConfig.rotationFrequencyDays))



    //Notify teams
    val firstPerson = peopleDao.loadPersonByUserId(setup.people.head.userId).get

    val teamsSendResult = Try(notifier.sendMessage(teamId, messageGenerator.generateMessage(firstPerson)))

    if (teamsSendResult.isFailure || !teamsSendResult.get) {
      if (teamsSendResult.isFailure) Log.error("Failed to send teams message", teamsSendResult.failed.get)
      TransactionAspectSupport.currentTransactionStatus.setRollbackOnly() //rollback because we may have made db changes by now
      return new ResponseEntity(ErrorMessageDTO(s"Failed to send a Teams message using your hook. Please check the Teams hook URL and message, then try again."), HttpStatus.BAD_REQUEST)
    }

    Log.info(s"Team '${setup.teamName}' created successfully")

    new ResponseEntity(SetupResponse(teamId), HttpStatus.CREATED)
  }


  @ApiOperation(
    value = "Get your team setup",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "OK")))
  @RequestMapping(value = Array("/setup"), method = Array(RequestMethod.GET))
  @Transactional
  def getSetup(@ApiIgnore @RequestHeader("user-id") userId: String): ResponseEntity[Any]  = {
    val teamId = peopleDao.loadPersonByUserId(userId)
      .getOrElse(return new ResponseEntity(ErrorMessageDTO(s"User ${userId} has no team assigned"), HttpStatus.BAD_REQUEST)).teamId

    val team = teamDao.getTeam(teamId).get
    val settings = settingsDao.settings(teamId).get

    val rotationDTO = RotationSetupDTO(settings.nextRotation, settings.rotationFrequency)
    val teamsDTO = TeamsSetupDTO(settings.hookUrl, settings.hookMessage)
    val peopleDTOs = peopleDao.loadAllPeopleOrdered(teamId).map(person => PersonSetupDTO(person.name, person.userId))

    new ResponseEntity(SetupDTO(team.name, rotationDTO, teamsDTO, peopleDTOs.toArray), HttpStatus.OK)
  }

}
