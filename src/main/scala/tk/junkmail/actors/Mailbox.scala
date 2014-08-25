package tk.junkmail.actors

import java.nio.ByteBuffer


import akka.actor.{Props, ActorLogging, Actor}
import akka.util.ByteString
import org.eclipse.jetty.websocket.api.Session
import tk.junkmail.model.{InitMailbox}

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

  implicit val executor = context.system.dispatcher
  val pinger = context.system.scheduler.schedule(0 milliseconds, 30 seconds, self, Ping)

  override def receive = {
    case SendEmail(data) => Unit //
    case SendInit => Try(session.getRemote) match {
      case Success(remote) => remote.sendStringByFuture(InitMailbox(emailAddress))
      case Failure(e) => self ! Mailbox.Stop
    }
    case Ping => Try(session.getRemote) match {
      case Success(remote) => remote.sendPing(ByteBuffer.wrap(Array(Byte.box(0))))
      case Failure(e) => self ! Mailbox.Stop
    }
    case Stop =>
      log.debug("Stopping mailbox={}", emailAddress);
      pinger.cancel()
      session.close()
      context stop self
  }
}
