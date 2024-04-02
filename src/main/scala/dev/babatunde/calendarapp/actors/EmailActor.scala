package dev.babatunde.calendarapp.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.sendgrid._
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.{Content, Email}
import dev.babatunde.calendarapp.model.EmailDetails

import scala.util.Try

sealed trait EmailCommand
case class SendEmail(emailDetails: EmailDetails, actorRef: ActorRef[Try[Response]]) extends EmailCommand

object EmailActor {

  def apply(sendgrid: SendGrid): Behavior[EmailCommand] = {
    Behaviors.receive((ctx, msg) => msg match {
      case SendEmail(emailDetails, actorRef) =>
        ctx.log.info("Sending email: {}", emailDetails)
        actorRef ! sendEmail(emailDetails,sendgrid)
        Behaviors.same
    })

  }

  private def sendEmail(emailDetails: EmailDetails, sendgrid: SendGrid): Try[Response] = Try{
    val receiverEmail = new Email(emailDetails.toEmail,emailDetails.toName)
    val content = new Content("text/plain", emailDetails.message)
    val subject = emailDetails.subject
    val senderEmail = new Email(emailDetails.fromEmail,emailDetails.fromName)
    val mail = new Mail(senderEmail, subject, receiverEmail, content)
    val request = new Request
    request.setMethod(Method.POST)
    request.setEndpoint("mail/send")
    request.setBody(mail.build)
    sendgrid.api(request)
  }


}
