package app.tasks

import app.business.{MessageGenerator, SlackNotifier}
import app.dao.{PeopleDao, SettingsDao, TeamDao}
import com.bayer.scala.transactions.TransactionalFunction
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Rotate(slackNotifier: SlackNotifier,
             settingsDao: SettingsDao,
             peopleDao: PeopleDao,
             txFunction: TransactionalFunction,
             messageGenerator: MessageGenerator,
             teamDao: TeamDao) {

  private val Log: Logger = LoggerFactory.getLogger(this.getClass)

  //run every 15 seconds
  @Scheduled(fixedRate = 15 * 1000, initialDelay = 1000)
  def doRotation(): Unit = {

    teamDao.getAllTeamsIds.foreach(teamId => {
      txFunction {

        Log.info(s"Checking rotation for team [$teamId]")

        //is current time >= rotate time
        if (!settingsDao.shouldRotate(teamId)) {
          Log.info("Done: don't need to rotate right now")
          return
        }

        //rotate and get the order pointer
        val orderPointer = settingsDao.rotate(teamId)

        Log.info(s"Rotated pointer to $orderPointer")

        //get person, using order pointer
        val person = peopleDao.loadPersonByOrder(teamId, orderPointer).get
        val message = messageGenerator.generateMessage(person)

        slackNotifier.sendMessage(teamId, message)

        Log.info(s"Done")

      }
    })
  }

}
