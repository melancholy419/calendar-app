package dev.babatunde.calendarapp.model

import java.time.Instant
import java.util.UUID

object ApiRequests {

  case class UserDetails(
                          firstName: String,
                          lastName: String,
                          emailAddress: String
                        )
  case class NotificationDetails(
                                  name: String,
                                  description: String,
                                  notificationDate: Instant,
                                  isDelivered: Boolean = false,
                                  userId: UUID
                                )

}
