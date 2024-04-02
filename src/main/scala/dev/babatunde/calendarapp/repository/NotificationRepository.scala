package dev.babatunde.calendarapp.repository

import dev.babatunde.calendarapp.model.Notification

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

trait NotificationRepository{
  def deleteById(id: UUID): Unit
  def addNewNotification(notification: Notification): Future[Notification]
  def updateNotification(notification: Notification): Future[Notification]
  def findAll: Future[Seq[Notification]]
  def findById(id: UUID): Future[Option[Notification]]
  def findAllByUserId(userId: UUID): Future[Seq[Notification]]
  def findDueNotifications(instant: Instant = Instant.now): Future[Seq[Notification]]

}
