package app.business

import app.config.AppProperties
import app.dao.{Person, SettingsDao}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Component

@Component
class MessageGenerator(settingsDao: SettingsDao,
                       appProperties: AppProperties) {

  private val Log: Logger = LoggerFactory.getLogger(this.getClass)

  def generateMessage(personOnSupport: Person): String = {
    val settings = settingsDao.settings(personOnSupport.teamId).get

    val message = settings.hookMessage
      .replaceAll("""\[name\]""", personOnSupport.name)
      .replaceAll("""\[cardurl\]""", appProperties.baseUrl + s"/on-call/${personOnSupport.teamId}")

    Log.info(s"Generated Support Message: ${message}")

    message
  }
}
