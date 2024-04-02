package dev.babatunde.calendarapp.endpoints

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.{Created, InternalServerError}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import dev.babatunde.calendarapp.model.ApiRequests.UserDetails
import dev.babatunde.calendarapp.model.User
import dev.babatunde.calendarapp.actors._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class UserEndpoints(actor: ActorRef[UserCommand])
                   (implicit timeout: Timeout,
                            scheduler: Scheduler,
                    executionContext: ExecutionContext) extends MyJsonProtocol with SprayJsonSupport{

  private def userById(userId: UUID): Future[Option[User]] = (actor ? { ref => FindById(userId, ref) }).flatMap(identity)

  private def findAllUsers: Future[Seq[User]] = actor ? FindAll

  private def findByEmailAddress(emailAddress: String): Future[Option[User]] =
    actor ? { ref => FindByEmailAddress(emailAddress, ref) }

  private def deleteById(id: UUID): Unit = actor ! DeleteUserById(id)

  private def updateUserDetails(id: UUID,userDetails: UserDetails) =
    (actor ? {ref => UpdateUser(id,userDetails,ref)}).flatMap(identity)

  private def addUser(addUserRequest: UserDetails): Future[User] = {
    val user = User(UUID.randomUUID(),addUserRequest.firstName,addUserRequest.lastName,addUserRequest.emailAddress)
    (actor ? {ref => AddUser(user, ref)}).flatMap(identity)
  }

  val userRoutes: Route = pathPrefix("users") {
    concat(
      pathEnd {
        get {
          onComplete(findAllUsers) {
            case Success(users) => complete(users)
            case Failure(ex) => complete(InternalServerError,"Unable to process request right now")
          }
        } ~
          post {
            entity(as[UserDetails]) { addUserRequest =>
              onComplete(addUser(addUserRequest)){
                case Success(user) => complete(Created, user)
                case Failure(ex) => complete(InternalServerError,"Unable to process request right now")
              }
            }
          }
      },
      path(JavaUUID) { userId =>
        get {
          onComplete(userById(userId)) {
            case Success(Some(user)) => complete(user)
            case Success(None)        => complete(StatusCodes.NotFound, "User not found")
            case Failure(ex)          => complete(StatusCodes.InternalServerError, s"Error occurred: ${ex.getMessage}")
          }
        } ~
          put{
            entity(as[UserDetails]){ userDetails =>
                onComplete(updateUserDetails(userId,userDetails)){
                  case Success(user) => complete(user)
                  case Failure(ex)          => complete(StatusCodes.InternalServerError, s"Error occurred: ${ex.getMessage}")
                }

            }
          } ~
          delete{
              deleteById(userId)
              complete(StatusCodes.OK)
          }
      }
    )
  }

}

object UserEndpoints{

  def apply(actor: ActorRef[UserCommand])
           (implicit timeout: Timeout,
            scheduler: Scheduler,
            executionContext: ExecutionContext): UserEndpoints =
    new UserEndpoints(actor)

}
