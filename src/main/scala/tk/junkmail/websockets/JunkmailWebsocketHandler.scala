package tk.junkmail.websockets

import akka.actor.ActorRef
import org.apache.commons.lang3.RandomStringUtils
import org.eclipse.jetty.websocket.api.Session
import org.slf4j.LoggerFactory
import tk.junkmail.actors.MessageDispatcher
import tk.junkmail.services.SessionService

import scala.util.{Failure, Success, Try}

/**
 * @author  Alexey Ponkin
 * @version 1, 24 Aug 2014
 */
object JunkmailWebsocketHandler{
   def apply(disp: ActorRef) = new JunkmailWebsocketHandler {

     val sess = SessionService()

     override implicit def sessionService = sess

     override implicit def dispatcher = disp
   }
}

trait JunkmailWebsocketHandler {

  final val LOG = LoggerFactory.getLogger(classOf[JunkmailWebsocketHandler])

  implicit def sessionService: SessionService

  implicit def dispatcher: ActorRef

  def onWebSocketError(id:String, cause: Throwable) = LOG.error("Web Socket Error {}", cause)

  def onWebSocketConnect(id:String, session: Session) = {
    sessionService.find(id) match {
      case Some((_id, email, createdAt)) => dispatcher ! MessageDispatcher.Subscribe(email, session)
      case None => generateEmail { newEmail =>
          sessionService.create(id, newEmail)
          dispatcher ! MessageDispatcher.Subscribe(newEmail, session)
      }
    }
  }

  def onWebSocketClose(id:String, statusCode: Int, reason: String) = {
    LOG.error("Web socket close with reason={} and code={}", reason, statusCode)
    sessionService.find(id) match {
      case Some((_id, email, createdAt)) =>
        dispatcher ! MessageDispatcher.Unsubscribe(email)
        sessionService.delete(id)
      case None => LOG.error("No session found for id={}", id)
    }
  }

  private def generateEmail[T](f: String => T, length:Int = 6):Unit = {
    if(length < 9) {
      Try(f(RandomStringUtils.randomAlphanumeric(length)+"@junkmail.tk")) match {
        case Success(v) => v
        case Failure(e) if length < 9 => generateEmail(f, length + 1)
        case Failure(e) => {
          LOG.error("can not generate email due to {}", e)
          throw e
        }
      }
    }
  }

}
