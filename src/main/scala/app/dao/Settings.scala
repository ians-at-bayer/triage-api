package app.dao

import java.time.Instant

case class Settings (slackHookUrl: String,
                     teamId: Int,
                     orderPointer: Int,
                     slackMessage: String,
                     nextRotation: Instant,
                     rotationFrequency: Int)
