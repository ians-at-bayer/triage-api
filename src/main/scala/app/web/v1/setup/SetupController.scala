package app.web.v1.setup

import app.dao.{PeopleDao, SettingsDao}
import app.web.v1.people.PeopleBusiness
import io.swagger.annotations._
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation._

@CrossOrigin
@RestController
@RequestMapping(Array("/v1"))
@Api(description = "setup the app for the first time ", tags = Array("setup"))
class SetupController(settingsDao: SettingsDao,
                      peopleDao: PeopleDao,
                      peopleBusiness: PeopleBusiness) {

  @ApiOperation(
    value = "Submit the first time setup",
    produces = "application/json"
  )
  @ApiResponses(value = Array(
    new ApiResponse(code = 204, message = "No Content")))
  @ResponseStatus(value = HttpStatus.NO_CONTENT) // This annotation necessary to keep swagger from listing 200 as a possible response code.
  @RequestMapping(value = Array("/setup"), method = Array(RequestMethod.POST), consumes = Array("application/json"))
  @Transactional
  def setup(@ApiParam(value = "setup") @RequestBody setup: SetupDTO): ResponseEntity[Any]  = {

    //remove current people
    peopleDao.loadAllPeopleOrdered()
      .foreach(person => peopleDao.removePerson(person.id))

    //add new people
    if (setup.people.length < 2)
      return new ResponseEntity("There must be at least two people for rotations to occur", HttpStatus.BAD_REQUEST)

    setup.people.foreach(people => peopleDao.insertPerson(people.name, people.slackId))

    //update slack config
    settingsDao.setSlackMessage(setup.slackConfig.slackMessage)
    settingsDao.setSlackHookURL(setup.slackConfig.slackHookUrl)

    //update rotation config
    settingsDao.setRotationFrequencyDays(setup.rotationConfig.rotationFrequencyDays)
    settingsDao.setNextRotation(setup.rotationConfig.nextRotationTime)

    //update base url
    settingsDao.setBaseUrl(setup.appBaseUrl)

    //reset the pointer to the first person in the list
    settingsDao.setPointer(0)

    new ResponseEntity(HttpStatus.NO_CONTENT)
  }

}
