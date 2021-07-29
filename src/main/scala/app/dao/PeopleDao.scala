package app.dao

import com.bayer.scala.jdbc.ScalaJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class PeopleDao(template: ScalaJdbcTemplate) {

  //
  // READ
  //

  /** Has more than one person */
  def peopleExist(teamId: Int): Boolean = template.queryForObject[Long](
    "select count(*) from people where team_id = ?", teamId).getOrElse(0L) > 1L

  def loadPerson(teamId: Int, id: Int) : Option[Person] =  {
    val sql = "select * from people where id = ? and team_id = ?"

    template.queryForObject(sql, new PersonRowMapper, id, teamId).headOption
  }

  def loadPersonByOrder(teamId: Int, orderPointer: Int) : Option[Person] = {
    val sql = "select * from people where order_number = ? and team_id = ?"

    template.queryForObject(sql, new PersonRowMapper, orderPointer, teamId).headOption
  }

  def loadAllPeopleOrdered(teamId: Int): Seq[Person] = {
    val sql = "select * from people where team_id = ? order by order_number asc"

    template.query[Person](sql, new PersonRowMapper, teamId)
  }

  def loadPersonByUserId(userId: String) : Option[Person] = {
    val sql = "select * from people where slack_id = ?"

    template.queryForObject(sql, new PersonRowMapper, userId.toLowerCase).headOption
  }

  //
  // INSERT
  //
  def insertPerson(name: String, slackId: String, teamId: Int): Unit = {
    val sql = "insert into people (name, team_id, slack_id, order_number) " +
      "select ?, ?, ?, COALESCE(max(order_number)+1, 0) from people where team_id = ?"

    template.update(sql, name, teamId, slackId.toLowerCase, teamId)
  }

  //
  // UPDATE
  //
  def changeOrder(teamId: Int, orderedPeopleIds: Seq[Int]) = {
    val sql = "update people set order_number = ? where id = ?"

    //make sure all people are distinct
    require(orderedPeopleIds.distinct.lengthCompare(orderedPeopleIds.length) == 0,
      "Ordered people IDs must be distinct")

    //make sure all people exist
    require(orderedPeopleIds.diff(loadAllPeopleOrdered(teamId).map(_.id)).isEmpty,
      "One or more people IDs do not exist")

    (0 to orderedPeopleIds.length-1)
      .zip(orderedPeopleIds)
      .foreach(item => template.update(sql, item._1, item._2))
  }

  //
  // DELETE
  //
  def removePerson(teamId: Int, id: Int): Unit = {
    val person = loadPerson(teamId, id)
      .getOrElse(throw new IllegalArgumentException(s"Person with ID $id does not exist"))

    val sql1 = "delete from people where id = ?"
    val sql2 = "update people set order_number = order_number - 1 where order_number > ? and team_id = ?"

    template.update(sql1, id)
    template.update(sql2, person.order, teamId)
  }

}
