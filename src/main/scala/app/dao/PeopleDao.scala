package app.dao

import com.bayer.scala.jdbc.ScalaJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class PeopleDao(template: ScalaJdbcTemplate) {

  //
  // READ
  //

  /** Has more than one person */
  def peopleExist(): Boolean = template.queryForObject[Long](
    "select count(*) from people").getOrElse(0L) > 1L

  def loadPerson(id: Int) : Option[Person] =  {
    val sql = "select * from people where id = ?"

    template.queryForObject(sql, new PersonRowMapper, id).headOption
  }

  def loadPersonByOrder(orderPointer: Int) : Option[Person] = {
    val sql = "select * from people where order_number = ?"

    template.queryForObject(sql, new PersonRowMapper, orderPointer).headOption
  }

  def loadAllPeopleOrdered(): Seq[Person] = {
    val sql = "select * from people order by order_number asc"

    template.query[Person](sql, new PersonRowMapper)
  }

  //
  // INSERT
  //
  def insertPerson(name: String, slackId: String): Unit = {
    val sql = "insert into people (name, slack_id, order_number) " +
      "select ?, ?, COALESCE(max(order_number)+1, 0) from people"

    template.update(sql, name, slackId)
  }

  //
  // UPDATE
  //
  def changeOrder(orderedPeopleIds: Seq[Int]) = {
    val sql = "update people set order_number = ? where id = ?"

    //make sure all people are distinct
    require(orderedPeopleIds.distinct.lengthCompare(orderedPeopleIds.length) == 0,
      "Ordered people IDs must be distinct")

    //make sure all people exist
    require(orderedPeopleIds.diff(loadAllPeopleOrdered.map(_.id)).isEmpty,
      "One or more people IDs do not exist")

    (0 to orderedPeopleIds.length-1)
      .zip(orderedPeopleIds)
      .foreach(item => template.update(sql, item._1, item._2))
  }

  //
  // DELETE
  //
  def removePerson(id: Int): Unit = {
    val person = loadPerson(id)
      .getOrElse(throw new IllegalArgumentException(s"Person with ID $id does not exist"))

    val sql1 = "delete from people where id = ?"
    val sql2 = "update people set order_number = order_number - 1 where order_number > ?"

    template.update(sql1, id)
    template.update(sql2, person.order)
  }

}
