package tk.junkmail

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.io.{Tcp, IO}
import tk.junkmail.actors.SessionCleaner
import tk.junkmail.jetty.WebSocketServer
import tk.junkmail.services.{SessionService, AbstractSystem, MainActors}
import tk.junkmail.websockets.JunkmailWebsocketHandler

/**
 * @author  Alexey Ponkin
 * @version 1, 24 Aug 2014
 */
object JunkmailSystem extends App with MainActors with AbstractSystem{



  implicit def system = ActorSystem("junkmail-service")

  private val sessionService = SessionService()
  private val js = new WebSocketServer(Configuration.portWs, JunkmailWebsocketHandler(dispatcher, sessionService))
  sys.addShutdownHook({system.shutdown();js.stop()})
  js.start()

  import scala.concurrent.duration._
  implicit val executor = system.dispatcher

  val sessionCleaner = system.actorOf(SessionCleaner.props(sessionService))
  system.scheduler.schedule(12 hours, 1 day, sessionCleaner, SessionCleaner.Clean) // clean expired sessions

  IO(Tcp) ! Tcp.Bind(emailEndpoint, new InetSocketAddress(Configuration.host, Configuration.portTcp))
}

object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("junkmail-service.host")
  val portTcp  = config.getInt("junkmail-service.ports.tcp")
  val portWs   = config.getInt("junkmail-service.ports.ws")
}
