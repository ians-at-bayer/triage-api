package app.dao

import org.springframework.jdbc.core.PreparedStatementCreator

import java.sql.{Connection, PreparedStatement, Statement}

class CreateTeamPreparedStatementCreator(teamName: String) extends PreparedStatementCreator {

  private val insert = "INSERT INTO teams VALUES (DEFAULT,?)"

  override def createPreparedStatement(conn: Connection): PreparedStatement = {
    val ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)
    ps.setObject(1, teamName)
    ps
  }
}
