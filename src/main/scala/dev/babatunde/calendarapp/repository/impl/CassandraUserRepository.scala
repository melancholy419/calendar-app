package dev.babatunde.calendarapp.repository.impl

import akka.stream.alpakka.cassandra.scaladsl.CassandraSession
import com.datastax.oss.driver.api.core.cql.Row
import dev.babatunde.calendarapp.model.User
import dev.babatunde.calendarapp.repository.UserRepository

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class CassandraUserRepository(implicit cassandraSession: CassandraSession)
                         extends UserRepository{

  override def deleteById(id: UUID): Future[Unit] = {
    cassandraSession.executeWrite("DELETE from users where id = ?",id)
    Future()
  }

  override def add(user: User): Future[User] = {
   val writeResult =  cassandraSession.executeWrite(
      """INSERT into Users(id,first_name,last_name,email_address)
        | values(?,?,?,?)""".stripMargin,
      user.id,
      user.firstName,
      user.lastName,
      user.emailAddress)
    writeResult.flatMap(_ => Future(user))
  }

  override def update(user: User): Future[User] = {
   val updateResult =  cassandraSession.executeWrite(
      """UPDATE Users
        |SET
        |first_name = ?,
        |last_name = ?,
        |email_address = ?
        |WHERE
        |id = ?
      """.stripMargin,
      user.firstName,
      user.lastName,
      user.emailAddress,
      user.id)
    updateResult.flatMap(_ => Future(user))
  }

  override def findById(id: UUID): Future[Option[User]] = {
    cassandraSession.selectOne(
        """
          |SELECT
          |id,
          |first_name,
          |last_name,
          |email_address
          |FROM users
          |WHERE
          |id = ?
          |""".stripMargin,id)
      .map{
        case Some(row) => Some(mapRowToUser(row))
        case _ => Option.empty
      }
  }

  override def findByEmailAddress(emailAddress: String): Future[Option[User]] = cassandraSession.selectOne(
      """
        |SELECT
        |id,
        |first_name,
        |last_name,
        |email_address
        |FROM users
        |WHERE
        |email_address = ?
        |ALLOW FILTERING
        |""".stripMargin, emailAddress)
    .map {
      case Some(row) => Some(mapRowToUser(row))
      case _ =>  Option.empty
    }

  override def findAll: Future[Seq[User]] = cassandraSession.selectAll(
    """
      |SELECT
      |id,
      |first_name,
      |last_name,
      |email_address
      |FROM users
      |""".stripMargin)
    .map(rows => mapRowsToUsers(rows))

  private def mapRowToUser(row: Row): User = {
    User(
      id = row.getUuid("id"),
      firstName = row.getString("first_name"),
      lastName = row.getString("last_name"),
      emailAddress = row.getString("email_address")
    )
  }

  private def mapRowsToUsers(rows: Seq[Row]): Seq[User] = {
  for{
      row <- rows
    } yield mapRowToUser(row)
  }

}
