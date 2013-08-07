package play.modules.mailer

import scala.util.Failure
import scala.util.Try

import javax.mail.Transport
import play.api.Play.current

class Mailer(val session: Session) {

  private def send(email: Email)(implicit transport: Transport): Try[Unit] =
    Try {
      val message = email createFor session
      transport.sendMessage(message, message.getAllRecipients)
    }.recoverWith {
      case cause => Failure(SendEmailException(email, cause))
    }

  private def tryWithTransport[T](code: Transport => T): Try[T] =
    Try {
      val transport = session.getTransport
      try {
        transport.connect()
        code(transport)
      } finally {
        transport.close()
      }
    }

  def sendEmail(email: Email): Try[Unit] =
    tryWithTransport { implicit transport =>
      send(email)
    }.flatten.recoverWith {
      case cause: SendEmailException => Failure(cause)
      case cause => Failure(SendEmailException(email, cause))
    }

  def sendEmails(emails: Seq[Email]): Try[Seq[Try[Unit]]] =
    tryWithTransport { implicit transport =>
      emails.map(send)
    }.recoverWith {
      case cause => Failure(SendEmailsException(emails, cause))
    }

  case class SendEmailException(email: Email, cause: Throwable) extends RuntimeException(cause)
  case class SendEmailsException(email: Seq[Email], cause: Throwable) extends RuntimeException(cause)

}

object Mailer extends Mailer(Session.fromConfiguration)


