package dev.babatunde.calendarapp.repository.impl

import dev.babatunde.calendarapp.model.User

import java.util.UUID


object TestData {

  val user1: User = User(UUID.randomUUID,"John","Doe","john.doe@hotmail.com")
  val user2: User = User(UUID.randomUUID(),"Jane","Doe","jane.doe@gmail.com")
  val user3: User = User(UUID.randomUUID(),"Lagbaja","Tamedo","lagbaja.tamedo@yahoo.com")

  val allUsers: Seq[User] = Seq(user1,user2,user3)

}
