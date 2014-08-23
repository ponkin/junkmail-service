package tk.junkmail.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp

/**
 * @author  Alexey Ponkin
 * @version 1, 13 Aug 2014
 */

object SocketService{
  def props(dispatcher: ActorRef): Props = Props(new SocketService(dispatcher))
}

class SocketService(val dispatcher: ActorRef) extends Actor with ActorLogging {
  override def receive = {
    case Tcp.Bound(localAddress) => log.debug("Tcp socket bound with local address="+localAddress);
    case Tcp.CommandFailed(_: Tcp.Bind) => context stop self
    case Tcp.Connected(remote, local) =>
      sender ! Tcp.Register(context.actorOf(SocketActor.props(dispatcher)))
  }
}

object SocketActor{
  def props(dispatcher: ActorRef): Props = Props(classOf[SocketActor], dispatcher)
}

class SocketActor(val dispatcher: ActorRef) extends Actor with ActorLogging {

  override def receive = {
    case Tcp.Received(data) => {
      val to = data takeWhile { _ != '\n'} dropRight 1
      dispatcher ! MessageDispatcher.Income(to, data.drop(to.length +2 ))
    }
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
