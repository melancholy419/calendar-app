package dev.babatunde.calendarapp.repository

import dev.babatunde.calendarapp.model.User

import java.util.UUID
import scala.concurrent.Future

trait UserRepository {
  def deleteById(id: UUID): Future[Unit]
  def add(user: User): Future[User]
  def update(user: User): Future[User]
  def findById(id: UUID): Future[Option[User]]
  def findByEmailAddress(emailAddress: String): Future[Option[User]]
  def findAll: Future[Seq[User]]

}
