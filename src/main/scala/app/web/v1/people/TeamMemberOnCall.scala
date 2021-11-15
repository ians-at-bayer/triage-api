package app.web.v1.people

import java.time.Instant

case class TeamMemberOnCall(name: String, teamName: String, userId: String, until: Instant)
