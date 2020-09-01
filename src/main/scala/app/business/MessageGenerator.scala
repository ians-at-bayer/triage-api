package app.business

import app.dao.{PeopleDao, Person, SettingsDao}
import org.springframework.stereotype.Component

@Component
class MessageGenerator(settingsDao: SettingsDao,
                       peopleDao: PeopleDao) {

  def generateMessage(personOnSupport: Person): String = {
      settingsDao.settings.slackMessage
      .replaceAll("""\[slackid\]""", personOnSupport.slackId)
      .replaceAll("""\[name\]""", personOnSupport.name)
      //.replaceAll("""\[oncallcardurl\]""", onCallCardUrl)
  }
}
