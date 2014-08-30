package tk.junkmail.actors

import java.nio.ByteBuffer
import java.util.Properties
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{ Session => JMSession }

import akka.actor.{Actor, ActorLogging, Props}
import akka.util.ByteString
import org.eclipse.jetty.websocket.api.Session

import scala.util.{Failure, Success, Try}

/**
 * @author  Alexey Ponkin
 * @version 1, 24 Aug 2014
 */
object Mailbox{
  sealed trait MailboxMessage
  case class SendEmail(data:ByteString) extends MailboxMessage
  case object Stop extends MailboxMessage
  case object Ping extends MailboxMessage
  case object SendInit extends MailboxMessage
  def props(emailAddress:String, session:Session) = Props(classOf[Mailbox], emailAddress, session)
}

class Mailbox(val emailAddress:String, val session:Session) extends Actor with ActorLogging{

  import tk.junkmail.actors.Mailbox._
  import tk.junkmail.model.ModelStringProtocol._
  import tk.junkmail.model._

import scala.concurrent.duration._

  implicit val executor = context.system.dispatcher
  val pinger = context.system.scheduler.schedule(10 seconds, 30 seconds, self, Ping)
  val s = JMSession.getDefaultInstance(new Properties())

  override def receive = {
    case SendEmail(data) =>
      log.debug("Send email length={}", data.length)
      val message = new MimeMessage(s, data.iterator.asInputStream)
      val email_body = body(message) match {
        case Some(text) => text
        case None => PlainEmail("", "")
      }

      val attachments = fetchAttachments(message)

      session.getRemote.sendStringByFuture(Envelope(InternetAddress.toString(message.getFrom), message.getSubject, message.getSentDate.getTime, email_body, attachments))

    case SendInit => Try(session.getRemote) match {
      case Success(remote) => remote.sendStringByFuture(InitMailbox(emailAddress))
      case Failure(e) => self ! Mailbox.Stop
    }
    case Ping => Try(session.getRemote) match {
      case Success(remote) => remote.sendPing(ByteBuffer.wrap(Array(Byte.box(0))))
      case Failure(e) => self ! Mailbox.Stop
    }
    case Stop =>
      log.debug("Stopping mailbox={}", emailAddress)
      pinger.cancel()
      context stop self
  }
}
