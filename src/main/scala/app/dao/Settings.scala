package app.dao

import java.time.Instant

case class Settings (slackHookUrl: String,
                     orderPointer: Int,
                     slackMessage: String,
                     baseUrl: String,
                     nextRotation: Instant,
                     rotationFrequency: Int)
