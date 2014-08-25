package tk.junkmail.actors

import akka.actor.{ActorRef, Props, Actor, ActorLogging}
import akka.io.Tcp

/**
 * @author  Alexey Ponkin
 * @version 1, 25 Aug 2014
 */
object EmailEndpoint{
 def props(dispatcher:ActorRef) = Props(classOf[EmailEndpoint], dispatcher)
}
class EmailEndpoint(val dispatcher:ActorRef) extends Actor with ActorLogging{
  override def receive = {
    case Tcp.CommandFailed(_: Tcp.Bind) => context stop self
    case Tcp.Connected(remote, local) =>
      sender ! Tcp.Register(context.actorOf(Props(classOf[SocketActor], dispatcher)))
  }
}

class SocketActor(val dispatcher:ActorRef) extends Actor with ActorLogging{
  override def receive = {
    case Tcp.Received(data) =>
      dispatcher ! MessageDispatcher.Email(data.takeWhile( _ != '\n').dropRight(1), data.dropWhile( _ != '\n').drop(1))
    case Tcp.PeerClosed => stop()
    case Tcp.ErrorClosed => stop()
    case Tcp.Closed => stop()
    case Tcp.ConfirmedClosed => stop()
    case Tcp.Aborted => stop()
  }
  private def stop() {
    context stop self
  }
}
