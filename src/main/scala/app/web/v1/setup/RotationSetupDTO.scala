package app.web.v1.setup

import java.time.Instant

case class RotationSetupDTO(nextRotationTime: Instant,
                            rotationFrequencyDays: Int)
