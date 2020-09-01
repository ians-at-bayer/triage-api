package app.dao

import java.sql.Timestamp
import java.time.Instant

import app.ISODateTimeUtil
import com.bayer.scala.jdbc.ScalaJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class SettingsDao(template: ScalaJdbcTemplate) {

  def shouldRotate: Boolean =
    template.queryForObject[Int](
      "select (now() >= next_rotation)::integer from settings").getOrElse(0) == 1

  def rotate: Int = {
    rotateOrderPointer
    progressRotationTimestamp

    settings.orderPointer
  }

  def settings : Settings = template.queryForObject(
    "select * from settings", new SettingsRowMapper).head

  def setPointer(orderNumber: Int): Unit = {
    val sql = "update settings set order_pointer = ?"

    template.update(sql, orderNumber)
  }

  def setSlackMessage(message: String): Unit = {
    val sql = "update settings set slack_message = ?"

    template.update(sql, message)
  }

  def setSlackHookURL(url: String): Unit = {
    val sql = "update settings set slack_hook_url = ?"

    template.update(sql, url)
  }

  def setRotationFrequencyDays(days: Int): Unit = {
    val sql = "update settings set rotation_frequency_days = ?"

    template.update(sql, days)
  }

  def setNextRotation(instant: Instant): Unit = {
    val sql = "update settings set next_rotation = ?"

    template.update(sql, new Timestamp(instant.toEpochMilli))
  }

  def setBaseUrl(url: String): Unit = {
    val sql = "update settings set base_url = ?"

    template.update(sql, url)
  }

  private def rotateOrderPointer : Int = template.update(
    "update settings set order_pointer = (select mod(order_pointer+1, max(order_number)+1) from people)")

  private def progressRotationTimestamp: Int = template.update(
    "update settings set next_rotation = (next_rotation + (rotation_frequency_days || 'days')::interval)")

}
