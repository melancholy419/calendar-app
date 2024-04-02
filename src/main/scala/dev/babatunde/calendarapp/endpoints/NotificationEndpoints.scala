package dev.babatunde.calendarapp.endpoints

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes.{Created, InternalServerError, NotFound}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import dev.babatunde.calendarapp.actors._
import dev.babatunde.calendarapp.model.ApiRequests.NotificationDetails
import dev.babatunde.calendarapp.model.Notification

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class NotificationEndpoints(actor: ActorRef[NotificationCommand])
                   (implicit timeout: Timeout,
                    scheduler: Scheduler) extends MyJsonProtocol with SprayJsonSupport{

  private def notificationById(id: UUID) =
    (actor ? { ref => FindNotificationById(id, ref) }).flatten

  private def findAllNotifications: Future[Seq[Notification]] = (actor ? FindAllNotifications).flatten

  private def findAllNotificationsByUserId(userId: UUID): Future[Seq[Notification]] =
    (actor ? { ref => FindNotificationsByUserId(userId, ref) }).flatten
  private def addNewNotification(notificationRequest: NotificationDetails): Future[Notification] =
    (actor ? {ref => AddNewNotification(notificationRequest,ref)}).flatten

  private def deleteNotificationById(id: UUID): Unit = actor ! DeleteNotificationById(id)

  val notificationRoutes: Route =
    pathPrefix("notifications") {
      concat(
        pathEnd {
          concat(
            get {
              onComplete(findAllNotifications) {
                case Success(notifications) => complete(notifications)
                case Failure(_) => complete(InternalServerError, "Unable to process request right now")
              }
            },
            post {
              entity(as[NotificationDetails]) { notificationDetails =>
                onComplete(addNewNotification(notificationDetails)) {
                  case Success(notification) => complete(Created, notification)
                  case Failure(_) => complete(InternalServerError, "Unable to process request right now")
                }
              }
            }
          )
        },
        path(JavaUUID) { id =>
          concat(
            get {
              onComplete(notificationById(id)) {
                case Success(value) => value match{
                  case Some(notification) => complete(notification)
                  case None => complete(NotFound,"Invalid notification")
                }
                case Failure(_) => complete(InternalServerError, "Unable to process request right now")
              }
            },
            delete{
              deleteNotificationById(id)
              complete("Successfully deleted")
              }
          )
        },
        path("users" / JavaUUID) { userId =>
          concat(
            get {
              onComplete(findAllNotificationsByUserId(userId)) {
                case Success(notifications) => complete(notifications)
                case Failure(_) => complete(InternalServerError, "Unable to process request right now")
              }
            }
          )
        }
      )
    }
}

object NotificationEndpoints {

  def apply(actor: ActorRef[NotificationCommand])
           (implicit timeout: Timeout,
            scheduler: Scheduler,
            executionContext: ExecutionContext): NotificationEndpoints =
    new NotificationEndpoints(actor)

}
