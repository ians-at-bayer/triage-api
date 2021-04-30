package app.business

import app.dao.SettingsDao
import com.bayer.scala.http.{Response, SimpleHttp}
import org.springframework.stereotype.Component

@Component
class SlackNotifier(httpClient: SimpleHttp, settingsDao: SettingsDao) {

  def sendMessage(teamId: Int, message: String): Boolean = {
    val slackHookUrl = settingsDao.settings(teamId).get.slackHookUrl

    httpClient.post(slackHookUrl,
      s"""{"text": "*On Call Support Person Change Notification*\n$message"}""")
      .map {
        case Response(200, _) => true
        case r => throw new IllegalStateException(s"unexpected response code=${r.code} and body='${r.body}'")
      }.get
  }

}

