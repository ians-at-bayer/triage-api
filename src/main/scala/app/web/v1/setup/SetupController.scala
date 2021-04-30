package app.web.v1.setup

import app.dao.{PeopleDao, Settings, SettingsDao, TeamDao}
import io.swagger.annotations._
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation._

import java.time.temporal.ChronoUnit
import java.time.Instant

@CrossOrigin
@RestController
@RequestMapping(Array("/v1"))
@Api(description = "setup your team for the first time ", tags = Array("setup"))
class SetupController(settingsDao: SettingsDao,
                      peopleDao: PeopleDao,
                      teamDao: TeamDao) {

  @ApiOperation(
    value = "Submit the first time setup for your team",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 201, message = "Team Created")))
  @ResponseStatus(value = HttpStatus.CREATED) // This annotation necessary to keep swagger from listing 200 as a possible response code.
  @RequestMapping(value = Array("/setup"), method = Array(RequestMethod.POST), consumes = Array("application/json"))
  @Transactional
  def setup(@ApiParam(value = "setup") @RequestBody setup: SetupDTO): ResponseEntity[Any]  = {

    //add new people
    if (setup.people.length < 2)
      return new ResponseEntity("There must be at least two people for rotations to occur", HttpStatus.BAD_REQUEST)

    //create the team and team name
    val teamId = teamDao.createTeam(setup.teamName)

    setup.people.foreach(people => peopleDao.insertPerson(people.name, people.slackId, teamId))

    val diff = Instant.now().until(setup.rotationConfig.nextRotationTime, ChronoUnit.NANOS)
    if (diff <= 0) return new ResponseEntity("The next rotation time must be later than the current time", HttpStatus.BAD_REQUEST)

    settingsDao.create(Settings(setup.slackConfig.slackHookUrl, teamId, 0,setup.slackConfig.slackMessage,
      setup.rotationConfig.nextRotationTime, setup.rotationConfig.rotationFrequencyDays))

    new ResponseEntity(SetupResponse(teamId), HttpStatus.CREATED)
  }

}
