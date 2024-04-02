package dev.babatunde.calendarapp.model

import java.time.Instant
import java.util.UUID


case class User(
                 id: UUID,
                 firstName: String,
                 lastName: String,
                 emailAddress: String
               )
case class Notification(
                         id: UUID,
                         name: String,
                         description: String,
                         notificationDate: Instant,
                         isDelivered: Boolean,
                         userId: UUID
                       )

case class EmailDetails(
    id: UUID,
    fromEmail: String,
    fromName: String,
    toEmail: String,
    toName: String,
    message: String,
    subject: String
)

case class EmailResponse(responseMessage: String, responseCode: String)

