package app.business

import app.dao.SettingsDao
import com.bayer.scala.http.{Response, SimpleHttp}
import org.springframework.stereotype.Component

@Component
class SlackNotifier(httpClient: SimpleHttp, settingsDao: SettingsDao) {

  def sendMessage(message: String): Boolean = {
    val slackHookUrl = settingsDao.settings.slackHookUrl

    httpClient.post(slackHookUrl,
      s"""{"text": "*TRIAGE ROTATIONS ALERT*\n$message"}""")
      .map {
        case Response(200, _) => true
        case r => throw new IllegalStateException(s"unexpected response code=${r.code} and body='${r.body}'")
      }.get
  }

}

