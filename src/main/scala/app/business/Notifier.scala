package app.business

import app.config.AppProperties
import app.dao.SettingsDao
import com.bayer.scala.http.{Response, SimpleHttp}
import org.springframework.stereotype.Component

@Component
class Notifier(httpClient: SimpleHttp, settingsDao: SettingsDao, appProperties: AppProperties) {

  def sendMessage(teamId: Int, message: String): Boolean = {
    val hookUrl = settingsDao.settings(teamId).get.hookUrl

    httpClient.post(hookUrl, hookJson(message, teamId))
      .map {
        case Response(200, _) => true
        case r => throw new IllegalStateException(s"unexpected response code=${r.code} and body='${r.body}'")
      }.get
  }

  private def hookJson(message: String, teamId: Int) =
    s"""
       |{
       |  "@context": "https://schema.org/extensions",
       |  "@type": "MessageCard",
       |  "themeColor": "0072C6",
       |  "title": "On Call Support Change Notification",
       |  "text": "$message",
       |  "potentialAction": [
       |    {
       |      "@type": "OpenUri",
       |      "name": "Contact Card",
       |      "targets": [
       |        { "os": "default", "uri": "${appProperties.baseUrl}/on-call/$teamId" }
       |      ]
       |    }
       |  ]
       |}
       |""".stripMargin

}

