package tk.junkmail.actors


import akka.actor._
import akka.util.ByteString
import org.eclipse.jetty.websocket.common.WebSocketRemoteEndpoint
import tk.junkmail.actors.MessageDispatcher.{Income, Register, Unregister}
import tk.junkmail.actors.{Mailbox => TKMailbox}

import scala.collection._

/**
 * @author  Alexey Ponkin
 * @version 1, 12 Aug 2014
 */

object MessageDispatcher {
  sealed trait Inbox
  case class Register(inboxName: String, client : WebSocketRemoteEndpoint) extends Inbox
  case class Unregister(inboxName: String) extends Inbox
  case class Income(to: ByteString, message: ByteString) extends Inbox

  def props: Props = Props(classOf[MessageDispatcher])
}


class MessageDispatcher extends Actor with ActorLogging{

  val clients = mutable.Map[String,ActorRef]()

  override def receive = {
    case Register(inboxName, client) => {
      val mailbox = context.actorOf(TKMailbox.props(client), inboxName)
      clients +=  (inboxName -> mailbox)
      log.debug("Registered inbox={} total={}", inboxName, clients.size)
    }
    case Unregister(inboxName) => {
      if(clients.contains(inboxName)){
        clients(inboxName) ! TKMailbox.Unsubscribe
        clients -= inboxName
        log.debug("Unregistered inbox={}", inboxName)
      }
    }
    case Income(to, data) => {
      clients get to.utf8String match {
        case Some(a) => a ! TKMailbox.Message(data)
        case None => log.debug("Key {} is not found", to.utf8String)
      }
    }

  }

}
