package app.dao

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

  private def rotateOrderPointer : Int = template.update(
    "update settings set order_pointer = (select mod(order_pointer+1, max(order_number)+1) from people)")

  private def progressRotationTimestamp: Int = template.update(
    "update settings set next_rotation = (next_rotation + (rotation_frequency_days || 'days')::interval)")

}
