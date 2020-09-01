package app.web.v1.people

import java.time.Instant

case class RotationConfigDTO(nextRotationTime: Option[Instant],
                             rotationFrequencyDays: Option[Int])
