package dev.babatunde.calendarapp.repository.impl

import akka.stream.alpakka.cassandra.scaladsl.CassandraSession
import com.datastax.oss.driver.api.core.cql.Row
import dev.babatunde.calendarapp.model.Notification
import dev.babatunde.calendarapp.repository.NotificationRepository
import org.slf4j.{Logger, LoggerFactory}

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CassandraNotificationRepository(implicit cassandraSession: CassandraSession)
                extends NotificationRepository {

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  override def addNewNotification(notification: Notification): Future[Notification] = {
    val writeResponse = cassandraSession.executeWrite(
      """INSERT INTO notifications(
        |id,
        |user_id,
        |name,
        |description,
        |notification_date,
        |is_delivered)
        |values(?,?,?,?,?,?)
        |""".stripMargin,
      notification.id,
      notification.userId,
      notification.name,
      notification.description,
      notification.notificationDate,
      notification.isDelivered: java.lang.Boolean
    )
    writeResponse.flatMap(_ => Future(notification))
  }
  override def updateNotification(notification: Notification): Future[Notification] = {
    log.info("Update notification::: [{}]", notification)
    val writeResponse = cassandraSession.executeWrite(
      """UPDATE notifications
        |SET
        |user_id = ?,
        |name = ?,
        |description = ?,
        |notification_date = ?,
        |is_delivered = ?
        |WHERE
        |id = ?
        |""".stripMargin,
      notification.userId,
      notification.name,
      notification.description,
      notification.notificationDate,
      notification.isDelivered: java.lang.Boolean,
      notification.id
    )
    writeResponse.flatMap(_ => Future(notification))
  }



  override def findAll: Future[Seq[Notification]] = cassandraSession.selectAll(
      """
        |SELECT
        |id,
        |user_id,
        |name,
        |description,
        |notification_date,
        |is_delivered
        |FROM
        |notifications
        |""".stripMargin)
    .map(rows => mapRowsToNotifications(rows))

  override def findById(id: UUID): Future[Option[Notification]] = cassandraSession.selectOne(
    """
      |SELECT
      |id,
      |user_id,
      |name,
      |description,
      |notification_date,
      |is_delivered
      |FROM
      |notifications
      |WHERE
      |id = ?
      |""".stripMargin,id)
    .map {
    case Some(row) => Some(mapRowToNotification(row))
    case _ => None
  }

  override def findAllByUserId(userId: UUID): Future[Seq[Notification]] =
    cassandraSession.selectAll(
        """
          |SELECT
          |id,
          |user_id,
          |name,
          |description,
          |notification_date,
          |is_delivered
          |FROM
          |notifications
          |WHERE
          |user_id = ?
          |ALLOW FILTERING
          |""".stripMargin,userId)
      .map(rows => mapRowsToNotifications(rows))


  override def deleteById(id: UUID): Unit = cassandraSession.executeWrite("DELETE from notifications where id = ?", id)

  override def findDueNotifications(instant: Instant = Instant.now): Future[Seq[Notification]] = {
    cassandraSession.selectAll(
        """
          |SELECT
          |id,
          |user_id,
          |name,
          |description,
          |notification_date,
          |is_delivered
          |FROM
          |notifications
          |WHERE
          |notification_date <= ? AND is_delivered = false
          |ALLOW FILTERING
          |""".stripMargin,instant)
      .map(rows => mapRowsToNotifications(rows))
  }
  private def mapRowToNotification(row: Row): Notification = {
    Notification(
      id = row.getUuid("id"),
      name = row.getString("name"),
      description = row.getString("description"),
      notificationDate = row.getInstant("notification_date"),
      isDelivered = row.getBoolean("is_delivered"),
      userId = row.getUuid("user_id")
    )
  }

  private def mapRowsToNotifications(rows: Seq[Row]): Seq[Notification] = {
    for {
      row <- rows
    } yield mapRowToNotification(row)
  }
}
