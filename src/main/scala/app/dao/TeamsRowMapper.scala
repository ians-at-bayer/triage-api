package app.dao

import org.springframework.jdbc.core.RowMapper

import java.sql.ResultSet

class TeamsRowMapper extends RowMapper[Team] {
  override def mapRow(rs: ResultSet, rowNum: Int): Team = {
    Team(
      rs.getInt("id"),
      rs.getString("name")
    )
  }
}
