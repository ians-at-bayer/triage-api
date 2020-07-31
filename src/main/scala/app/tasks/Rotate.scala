package app.tasks

import app.business.SlackNotifier
import app.dao.{PeopleDao, Person, SettingsDao}
import com.bayer.scala.transactions.TransactionalFunction
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Rotate(slackNotifier: SlackNotifier,
             settingsDao: SettingsDao,
             peopleDao: PeopleDao,
             txFunction: TransactionalFunction) {

  private val Log: Logger = LoggerFactory.getLogger(this.getClass)

  @Scheduled(fixedRate = 20 * 1000, initialDelay = 1000)
  def doRotation(): Unit = {
    txFunction {

      Log.info("Checking if I should rotate")

      if (!peopleDao.hasPeople) {
        Log.info("Done: not enough people to rotate")
        return
      }

      //is current time >= rotate time
      if (!settingsDao.shouldRotate) {
        Log.info("Done: don't need to rotate right now")
        return
      }

      //rotate and get the order pointer
      val orderPointer = settingsDao.rotate

      Log.info(s"Rotated pointer to $orderPointer")

      //get person, using order pointer
      val person = peopleDao.getPersonByOrder(orderPointer).get

      Log.info(s"Sending a Slack message to ${person.name}")

      slackNotifier.sendMessage(s"<@${person.slackId}> (${person.name}) is currently on support")

      Log.info(s"Done")

    }
  }

}
