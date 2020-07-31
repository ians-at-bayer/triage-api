package app.dao

import com.bayer.scala.jdbc.ScalaJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class PeopleDao(template: ScalaJdbcTemplate) {

  /** Has more than one person */
  def hasPeople(): Boolean = template.queryForObject[Long](
    "select count(*) from people").getOrElse(0L) > 1L

  def getPersonByOrder(orderPointer: Int) : Option[Person] = {
    val sql = "select * from people where order_number = ?"

    template.queryForObject(sql, new PersonRowMapper, orderPointer).headOption
  }

}
