package app.dao

import java.sql.ResultSet

import org.springframework.jdbc.core.RowMapper

class PersonRowMapper extends RowMapper[Person] {
  override def mapRow(rs: ResultSet, rowNum: Int): Person = {
    Person(
      rs.getInt("id"),
      rs.getString("name"),
      rs.getString("slack_id"),
      rs.getInt("order_number")
    )
  }
}
