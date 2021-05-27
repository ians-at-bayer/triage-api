package app.dao

import com.bayer.scala.jdbc.{ScalaJDBC, ScalaJdbcTemplate}
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository

@Repository
class TeamDao(template: ScalaJdbcTemplate) {

  def createTeam(teamName: String): Int = {
    val keyHolder = new GeneratedKeyHolder()
    template.update(new CreateTeamPreparedStatementCreator(teamName), keyHolder)

    ScalaJDBC.getIntKey(keyHolder, "id")
  }

  def updateTeamName(teamId: Int, teamName: String) = template
    .update("update teams set name = ? where id = ?", teamName, teamId)

  def getTeam(id: Int): Option[Team] = template.query[Team]("select * from teams where id = ?", new TeamsRowMapper, id).headOption

  def getAllTeamsIds: Seq[Int] = template.queryForSeq[Int]("select id from teams")

}
