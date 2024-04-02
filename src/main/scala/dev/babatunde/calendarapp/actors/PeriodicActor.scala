package dev.babatunde.calendarapp.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import dev.babatunde.calendarapp.repository.NotificationRepository
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object PeriodicActor {

  sealed trait TimerMsg

  private case object Tick extends TimerMsg

  def apply(notificationRepository: NotificationRepository)
           (implicit kafkaProducer: KafkaProducer[String,String],
            ec: ExecutionContext): Behavior[TimerMsg] = {
    Behaviors.setup { context =>
      val log = context.log
      Behaviors.withTimers { timers =>
        timers.startTimerAtFixedRate(Tick, 30.seconds)
        Behaviors.receive { (_, msg) =>
          msg match {
            case Tick =>
              notificationRepository.findDueNotifications().onComplete{
                case Success(notifications) =>
                  notifications.foreach{
                  notification =>
                    val key = notification.id.toString
                    val value = notification.asJson.noSpaces
                    val record  = new ProducerRecord("notifications",key,value)
                    val sendToTopic = kafkaProducer.send(record)
                    if(sendToTopic.isDone) {
                      val updatedNotification = notification.copy(isDelivered = true)
                      notificationRepository.updateNotification(updatedNotification).onComplete{
                        case Success(value) => log.info("Successfully delivered notification")
                        case Failure(ex) => log.error("Exception occurred because: [{}]", ex.getMessage, ex)
                      }
                    }
                }
                case Failure(ex) => log.error("Exception occurred while processing data: [{}]",ex.getMessage, ex)
              }
              Behaviors.same
          }
        }
      }
    }
  }
}