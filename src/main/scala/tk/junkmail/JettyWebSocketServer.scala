package tk.junkmail

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import akka.actor.ActorRef
import com.typesafe.scalalogging.slf4j.Logger
import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.websocket.api.{Session, WebSocketAdapter}
import org.eclipse.jetty.websocket.common.WebSocketRemoteEndpoint
import org.eclipse.jetty.websocket.servlet._
import org.slf4j.LoggerFactory
import tk.junkmail.actors.MessageDispatcher
import tk.junkmail.model.JunkmailCookie
import tk.junkmail.services.SessionService

/**
 * @author  Alexey Ponkin
 * @version 1, 12 Aug 2014
 */
object JettyWebSocketServer{
  def apply(port : Int, dispatcher: ActorRef, sessionsService: SessionService) = new JettyWebSocketServer(port, dispatcher, sessionsService)
}
class JettyWebSocketServer(val port : Int, val dispatcher : ActorRef, val sessionsService: SessionService){

  val server = new Server();
  val connector = new ServerConnector(server);
  connector.setPort(port);
  server.addConnector(connector);

  // Setup the basic application "context" for this application at "/"
  // This is also known as the handler tree (in jetty speak)
  val context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS)
  context.setContextPath("/")
  server.setHandler(context)

  // Add a websocket to a specific path spec
  val holderEvents = new ServletHolder("ws-events", JunkmailServlet(dispatcher, sessionsService))
  context.addServlet(holderEvents, "/messages/*")

  def start = {
    server.start()
    server.join()
  }

  def stop = server.stop();

}

object JunkmailServlet{
  def apply(dispatcher: ActorRef, sessionsService: SessionService) = new JunkmailServlet(dispatcher, sessionsService)
}

class JunkmailServlet(val dispatcher: ActorRef, val sessionsService: SessionService) extends WebSocketServlet{

  val log = Logger(LoggerFactory.getLogger(classOf[JunkmailServlet]))

  final override def configure(factory:WebSocketServletFactory) = {
    factory.getPolicy.setIdleTimeout(60000); // Set connection idle timeout to 60 sec
    factory.setCreator(JunkmailCreator(dispatcher, sessionsService))
  }

  final override def service(request: HttpServletRequest, response: HttpServletResponse) = {
    request.getCookies match {
      case _ => response.addCookie(JunkmailCookie(java.util.UUID.randomUUID().toString).asCookie)
    }
    log.debug("Set cookie")
    super.service(request, response)
  }
}

object JunkmailCreator{
  def apply(dispatcher: ActorRef, sessionsService: SessionService) = new JunkmailCreator(dispatcher, sessionsService)
}

class JunkmailCreator(val dispatcher: ActorRef, val sessionsService: SessionService) extends WebSocketCreator{
  override def createWebSocket(req: ServletUpgradeRequest, resp: ServletUpgradeResponse)= {
    val cookies = req.getHttpServletRequest.getCookies
    JunkmailAdapter(cookies(0).getValue, dispatcher, sessionsService)
  }
}

object JunkmailAdapter{
  def apply(id: String, dispatcher: ActorRef, sessionsService: SessionService) = new JunkmailAdapter(id, dispatcher, sessionsService)
}
class JunkmailAdapter(val id: String, val dispatcher: ActorRef, val sessionsService: SessionService) extends WebSocketAdapter {


  val log = Logger(LoggerFactory.getLogger(classOf[JunkmailAdapter]))

  final override def onWebSocketConnect(sess: Session) = {
    val (s_id, s_inboxName) = sessionsService.create(id)
    log.debug("Create session for id={} and inboxName={}", s_id, s_inboxName)
    dispatcher ! MessageDispatcher.Register(s_inboxName, sess.getRemote.asInstanceOf[WebSocketRemoteEndpoint])
    sess.getRemote.sendString(s"""{"id":"$s_id","inboxName":"$s_inboxName"}""")
  }

  final override def onWebSocketClose(statusCode: Int, reason: String) = {
    log.debug("onWebSocketClose with code={} and reason {}", Int.box(statusCode), reason)
    sessionsService.findById(id) match {
      case Some((_, s_inboxName)) => {
        log.debug("Delete session for id={} anf inboxName={}", id, s_inboxName)
        sessionsService.delete(id)
        dispatcher ! MessageDispatcher.Unregister(s_inboxName)
      }
      case None => {
        log.error("No session found for id={}", id)
      }
    }
  }

  final override def onWebSocketBinary(payload: Array[Byte], offset: Int, len: Int) = log.debug("onWebSocketBinary")

  final override def onWebSocketText(message: String) = {
    log.debug("onWebSocketText with text={}", message)
  }

  final override def onWebSocketError(cause: Throwable) = log.debug("onWebSocketError with error={}", cause)
}
