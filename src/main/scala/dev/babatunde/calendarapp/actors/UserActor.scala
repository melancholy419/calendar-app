package dev.babatunde.calendarapp.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import dev.babatunde.calendarapp.model.ApiRequests.UserDetails
import dev.babatunde.calendarapp.model.User
import dev.babatunde.calendarapp.repository.UserRepository

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

sealed trait UserCommand
case class AddUser(user: User, actorRef: ActorRef[Future[User]]) extends UserCommand
case class UpdateUser(id: UUID, user: UserDetails, actorRef: ActorRef[Future[User]]) extends UserCommand
case class FindById(id: UUID, actorRef: ActorRef[Future[Option[User]]]) extends UserCommand
case class FindByEmailAddress(emailAddress: String, actorRef: ActorRef[Option[User]]) extends UserCommand

case class DeleteUserById(id: UUID) extends UserCommand
case class FindAll(actorRef: ActorRef[Seq[User]]) extends UserCommand

object UserService{
  def apply(userRepository: UserRepository): Behavior[UserCommand] = {
    Behaviors.receive( (ctx,message) => message match {
      case AddUser(user,replyTo) =>
        ctx.log.info("Adding new user with request: {}", user)
        replyTo ! userRepository.add(user)
        Behaviors.same

      case UpdateUser(id,userDetails,replyTo) =>
        ctx.log.info("updating user with id: {} request: {}",id, userDetails)
        val user = User(id,userDetails.firstName,userDetails.lastName,userDetails.emailAddress)
        replyTo !  Future(userRepository.update(user)).flatMap(identity)
        Behaviors.same

      case FindById(id,replyTo) =>
        ctx.log.info("Finding user with id: {}",id)
        replyTo ! userRepository.findById(id)
        Behaviors.same

      case FindByEmailAddress(emailAddress,replyTo) =>
        ctx.log.info("Finding user with email: {}",emailAddress)
        userRepository.findByEmailAddress(emailAddress).onComplete {
          case Success(value) => replyTo ! value
          case Failure(exception) =>
            ctx.log.error("Error occurred while trying to find user by email address: {} because: {}",emailAddress, exception.getMessage)
            replyTo ! Option.empty
        }
        Behaviors.same
      case DeleteUserById(id) =>
        ctx.log.info("Deleting user with id: {}", id)
        userRepository.deleteById(id)
        Behaviors.same

      case FindAll(replyTo) =>
        ctx.log.info("Find all users")
        userRepository.findAll.onComplete {
          case Success(value) =>  replyTo ! value
          case Failure(_) => Seq.empty
        }
        Behaviors.same
    })
  }
}
