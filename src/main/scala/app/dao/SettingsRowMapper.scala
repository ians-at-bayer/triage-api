package app.dao

import java.sql.ResultSet

import org.springframework.jdbc.core.RowMapper

class SettingsRowMapper extends RowMapper[Settings] {
  override def mapRow(rs: ResultSet, rowNum: Int): Settings = {
    Settings(
      rs.getString("slack_hook_url"),
      rs.getInt("order_pointer")
    )
  }
}
