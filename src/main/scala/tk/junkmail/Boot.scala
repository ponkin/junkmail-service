package tk.junkmail


import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.io.{Tcp, IO}
import spray.can.Http
import tk.junkmail.actors.{JunkmailApi, AbstractSystem, MainActors}
import tk.junkmail.services.SessionService


/**
 * @author  Alexey Ponkin
 * @version 1, 08 Aug 2014
 */
object Boot extends App with MainActors with JunkmailApi{

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("junkmail-system")

  val sessionsService = SessionService()
  val server = JettyWebSocketServer(Configuration.portWs, dispatcher, sessionsService);
  sys.addShutdownHook({system.shutdown; server.stop})
  //IO(Http) ! Http.Bind(staticService, Configuration.host, port = Configuration.portHttp)
  IO(Tcp) ! Tcp.Bind(socketService, new InetSocketAddress(Configuration.host, Configuration.portTcp))
  server.start
}

object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("junkmail.host")
  val portHttp = config.getInt("junkmail.ports.http")
  val portTcp  = config.getInt("junkmail.ports.tcp")
  val portWs   = config.getInt("junkmail.ports.ws")
}
