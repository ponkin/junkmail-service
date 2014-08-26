package tk.junkmail.actors

import akka.actor.{Props, ActorLogging, Actor}
import akka.util.ByteString
import org.eclipse.jetty.websocket.api.Session


/**
 * @author  Alexey Ponkin
 * @version 1, 24 Aug 2014
 */
object MessageDispatcher{
  sealed trait MessageDispatcherMessage
  case class Unsubscribe(id:String) extends MessageDispatcherMessage
  case class Subscribe(id:String, session:Session) extends MessageDispatcherMessage
  case class Email(to:ByteString, data:ByteString) extends MessageDispatcherMessage

  def props = Props(classOf[MessageDispatcher])
}

class MessageDispatcher extends Actor with ActorLogging{

  import MessageDispatcher._

  override def receive = {
    case Unsubscribe(id) => context.actorSelection(id) ! Mailbox.Stop
    case Subscribe(id, session) => context.actorOf(Mailbox.props(id, session), id) ! Mailbox.SendInit
    case Email(to, data) =>
      log.debug("Received data for={}", to.utf8String)
      context.actorSelection(to.utf8String) ! Mailbox.SendEmail(data)
  }
}
