package app.dao

import java.time.Instant

case class Settings (hookUrl: String,
                     teamId: Int,
                     orderPointer: Int,
                     hookMessage: String,
                     nextRotation: Instant,
                     rotationFrequency: Int)
