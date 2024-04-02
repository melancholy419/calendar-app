package dev.babatunde.calendarapp

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.concat
import akka.stream.alpakka.cassandra.CassandraSessionSettings
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSession, CassandraSessionRegistry}
import akka.util.Timeout
import dev.babatunde.calendarapp.actors.{NotificationService, PeriodicActor, UserService}
import dev.babatunde.calendarapp.configs.KafkaConfig
import dev.babatunde.calendarapp.endpoints.{NotificationEndpoints, UserEndpoints}
import dev.babatunde.calendarapp.repository.impl.{CassandraNotificationRepository, CassandraUserRepository}
import org.apache.kafka.clients.producer.KafkaProducer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.io.StdIn.readLine



object Main extends App {

  implicit val actorSystem: ActorSystem[Nothing] =
    ActorSystem(Behaviors.empty,"cassandraApp")
  private val sessionSettings = CassandraSessionSettings()
  implicit val cassandraSession: CassandraSession =
    CassandraSessionRegistry.get(actorSystem)
      .sessionFor(sessionSettings)
  val userRepository = new CassandraUserRepository
  private val userService = UserService(userRepository)
  implicit val timeout: Timeout = Timeout(3.seconds)
  implicit val executionContext = actorSystem.executionContext
  val notificationRepository = new CassandraNotificationRepository
  implicit val kafkaProducerConfig: KafkaProducer[String, String] = KafkaConfig.config
  private val notificationService = NotificationService(notificationRepository)
  val periodicActor = actorSystem.systemActorOf(PeriodicActor(notificationRepository), "PeriodicActor")
  val userActor = actorSystem.systemActorOf(userService, "UserActorSystem")
  private val notificationActor = actorSystem.systemActorOf(notificationService,"NotificationActor")
  implicit val scheduler: Scheduler = actorSystem.scheduler


  private val routes = concat(UserEndpoints(userActor).userRoutes,
                      NotificationEndpoints(notificationActor)
                        .notificationRoutes
                      )

  private val bindingFuture = Http()
                        .newServerAt("localhost", 9091)
                        .bind(routes)
  println(s"Server online at http://localhost:9091/")
  readLine()

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => actorSystem.terminate())
}
