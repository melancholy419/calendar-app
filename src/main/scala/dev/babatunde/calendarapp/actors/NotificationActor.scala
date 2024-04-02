package dev.babatunde.calendarapp.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import dev.babatunde.calendarapp.model.ApiRequests.NotificationDetails
import dev.babatunde.calendarapp.model.Notification
import dev.babatunde.calendarapp.repository.NotificationRepository
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import java.util.UUID
import scala.concurrent.Future


sealed trait NotificationCommand
  case class FindAllNotifications(replyTo: ActorRef[Future[Seq[Notification]]]) extends NotificationCommand
  case class FindNotificationsByUserId(userId: UUID, replyTo: ActorRef[Future[Seq[Notification]]]) extends NotificationCommand
  case class FindNotificationById(id: UUID, replyTo: ActorRef[Future[Option[Notification]]]) extends NotificationCommand
  case class AddNewNotification(notificationRequest: NotificationDetails, replyTo: ActorRef[Future[Notification]]) extends NotificationCommand
  case class UpdateNotification(id: UUID, notificationRequest: NotificationDetails, replyTo: ActorRef[Future[Notification]]) extends NotificationCommand
  case class DeleteNotificationById(id: UUID) extends NotificationCommand
object NotificationService {

  def apply(notificationRepository: NotificationRepository):
                  Behavior[NotificationCommand] = {

    Behaviors.receive((ctx, msg) => msg match {
      case FindAllNotifications(replyTo) =>
        replyTo ! notificationRepository.findAll
        Behaviors.same

      case FindNotificationsByUserId(userId, replyTo) =>
        ctx.log.info("Trying to find all notifications by userId: {}",userId)
        replyTo ! notificationRepository.findAllByUserId(userId)
        Behaviors.same

      case FindNotificationById(id, replyTo) =>
        replyTo ! notificationRepository.findById(id)
        Behaviors.same

      case AddNewNotification(notificationRequest, replyTo) =>
        ctx.log.info("Adding new notification with request: {}", notificationRequest)
        val notification = Notification(
          id = UUID.randomUUID(),
          name = notificationRequest.name,
          description = notificationRequest.description,
          notificationDate = notificationRequest.notificationDate,
          isDelivered = notificationRequest.isDelivered,
          userId = notificationRequest.userId
        )
        replyTo ! notificationRepository.addNewNotification(notification)
        Behaviors.same

      case UpdateNotification(id, notificationRequest, replyTo) =>
        ctx.log.info("Trying to update notification with id: {} and details: {} ",id,notificationRequest)
        val notification = Notification(
          id,
          notificationRequest.name,
          notificationRequest.description,
          notificationRequest.notificationDate,
          notificationRequest.isDelivered,
          notificationRequest.userId
        )
        replyTo ! notificationRepository.updateNotification(notification)
        Behaviors.same
      case DeleteNotificationById(id) =>
        notificationRepository.deleteById(id)
        Behaviors.same
    }
    )
  }

}
