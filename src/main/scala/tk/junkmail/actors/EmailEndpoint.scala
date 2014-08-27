package tk.junkmail.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.util.ByteString

/**
 * @author  Alexey Ponkin
 * @version 1, 25 Aug 2014
 */
object EmailEndpoint{
 def props(dispatcher:ActorRef) = Props(classOf[EmailEndpoint], dispatcher)
}
class EmailEndpoint(val dispatcher:ActorRef) extends Actor with ActorLogging{
  override def receive = {
    case b @ Bound(localAddress) => log.debug("Bound to local address")
    case CommandFailed(_: Bind) => context stop self
    case c @ Connected(remote, local) =>
      sender ! Register(context.actorOf(Props(classOf[SocketActor], dispatcher)), keepOpenOnPeerClosed = true)
  }
}

class SocketActor(val dispatcher:ActorRef) extends Actor with ActorLogging{

  val buffer = ByteString.newBuilder

  override def receive = {

    case Received(data) =>
      log.debug("Data length={}", data.length)
      buffer.append(data)
    case PeerClosed     =>
      val message = buffer.result()
      dispatcher ! MessageDispatcher.Email(message.takeWhile( _ != '\n'), message.dropWhile( _ != '\n').drop(1))
      context stop self
  }


}
