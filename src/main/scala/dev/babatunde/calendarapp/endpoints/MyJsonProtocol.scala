package dev.babatunde.calendarapp.endpoints

import dev.babatunde.calendarapp.model.ApiRequests.{NotificationDetails, UserDetails}
import dev.babatunde.calendarapp.model.{Notification, User}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat, deserializationError}

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

trait MyJsonProtocol extends DefaultJsonProtocol {
  implicit object UuidJsonFormat extends JsonFormat[UUID] {
    def write(uuid: UUID): JsValue = JsString(uuid.toString)

    def read(value: JsValue): UUID = value match {
      case JsString(str) => UUID.fromString(str)
      case _             => deserializationError("UUID expected")
    }
  }

  implicit object InstantFormat extends JsonFormat[Instant] {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT

    def write(obj: Instant): JsValue = JsString(formatter.format(obj))

    def read(json: JsValue): Instant = json match {
      case JsString(value) => Instant.from(formatter.parse(value))
      case _               => deserializationError("Instant expected")
    }
  }
  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User)
  implicit val userDetailsFormat: RootJsonFormat[UserDetails] = jsonFormat3(UserDetails)
  implicit val notificationDetailsFormat: RootJsonFormat[NotificationDetails] = jsonFormat5(NotificationDetails)
  implicit val notificationFormat: RootJsonFormat[Notification] = jsonFormat6(Notification)
}