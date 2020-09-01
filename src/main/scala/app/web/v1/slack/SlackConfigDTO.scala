package app.web.v1.slack

case class SlackConfigDTO(slackHookUrl: Option[String], slackMessage: Option[String])
