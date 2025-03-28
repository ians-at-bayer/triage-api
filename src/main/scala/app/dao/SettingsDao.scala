package app.dao

import java.sql.Timestamp
import java.time.Instant

import com.bayer.scala.jdbc.ScalaJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class SettingsDao(template: ScalaJdbcTemplate, teamDao: TeamDao) {

  def shouldRotate(teamId: Int): Boolean =
    template.queryForObject[Int](
      "select (now() >= next_rotation)::integer from settings where team_id = ?", teamId).getOrElse(0) == 1

  def rotate(teamId: Int): Int = {
    rotateOrderPointer(teamId)
    progressRotationTimestamp(teamId)

    settings(teamId).get.orderPointer
  }

  def create(settings: Settings): Unit = {
    val sql = "insert into settings (team_id, hook_url, order_pointer, next_rotation, rotation_frequency_days, message) values (?,?,?,?,?,?)"

    template.update(sql, settings.teamId, settings.hookUrl, settings.orderPointer, new Timestamp(settings.nextRotation.toEpochMilli),
      settings.rotationFrequency, settings.hookMessage)
  }

  def settings(teamId: Int) : Option[Settings] = template.queryForObject(
    "select * from settings where team_id = ?", new SettingsRowMapper, teamId).headOption

  def setPointer(teamId: Int, orderNumber: Int): Unit = {
    val sql = "update settings set order_pointer = ? where team_id = ?"

    template.update(sql, orderNumber, teamId)
  }

  def setMessage(teamId: Int, message: String): Unit = {
    val sql = "update settings set message = ? where team_id = ?"

    template.update(sql, message, teamId)
  }

  def setHookURL(teamId: Int, url: String): Unit = {
    val sql = "update settings set hook_url = ? where team_id = ?"

    template.update(sql, url, teamId)
  }

  def setRotationFrequencyDays(teamId: Int, days: Int): Unit = {
    val sql = "update settings set rotation_frequency_days = ? where team_id = ?"

    template.update(sql, days, teamId)
  }

  def setNextRotation(teamId: Int, instant: Instant): Unit = {
    val sql = "update settings set next_rotation = ? where team_id = ?"

    template.update(sql, new Timestamp(instant.toEpochMilli), teamId)
  }

  def setChatbotId(teamId: Int, chatbotId: Option[String]): Unit = {
    val sql = "update settings set chatbot_id = ? where team_id = ?"

    template.update(sql, chatbotId.orNull, teamId)
  }

  private def rotateOrderPointer(teamId: Int) : Int = template.update(
    "update settings set order_pointer = (select mod(order_pointer+1, max(order_number)+1) from people where team_id = ?) where team_id = ?", teamId, teamId)

  private def progressRotationTimestamp(teamId: Int): Int = template.update(
    "update settings set next_rotation = (next_rotation + (rotation_frequency_days || 'days')::interval) where team_id = ?", teamId)

}
