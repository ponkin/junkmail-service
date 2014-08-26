package tk.junkmail.actors

import java.nio.ByteBuffer
import java.util.Properties
import javax.mail.internet.MimeMessage


import akka.actor.{Props, ActorLogging, Actor}
import akka.util.ByteString
import org.eclipse.jetty.websocket.api.Session
import javax.mail.{Session => JMSession, Address}
import tk.junkmail.model.InitMailbox

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

  import Mailbox._
  import scala.concurrent.duration._
  import tk.junkmail.model.ModelStringProtocol._
  import tk.junkmail.model._

  val s = JMSession.getDefaultInstance(new Properties())
  implicit val executor = context.system.dispatcher
  val pinger = context.system.scheduler.schedule(10 seconds, 30 seconds, self, Ping)

  override def receive = {
    case SendEmail(data) =>
      log.debug("Send email for={}", emailAddress)
      val message = new MimeMessage(s, data.iterator.asInputStream)
      val from = message.getFrom match {
        case a:Array[Address] => a.headOption match{
          case Some(a) => a.toString
          case None => "unknown@unknown"
        }
        case _ => "unknown@unknown"

      }
      session.getRemote.sendStringByFuture(Envelope(from, message.getSubject, message.getSentDate.getTime, body(message).get, fetchAttachments(message)))

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
      session.close()
      context stop self
  }
}
