package app.dao

import com.bayer.scala.jdbc.ScalaJDBC

import java.sql.ResultSet
import org.springframework.jdbc.core.RowMapper

class SettingsRowMapper extends RowMapper[Settings] {
  override def mapRow(rs: ResultSet, rowNum: Int): Settings = {
    Settings(
      rs.getString("hook_url"),
      rs.getInt("team_id"),
      rs.getInt("order_pointer"),
      rs.getString("message"),
      rs.getTimestamp("next_rotation").toInstant,
      rs.getInt("rotation_frequency_days"),
      ScalaJDBC.getOptionString(rs, "chatbot_id")
    )
  }
}
