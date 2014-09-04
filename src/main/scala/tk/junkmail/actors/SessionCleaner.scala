package tk.junkmail.actors

import akka.actor.{Props, ActorLogging, Actor}
import tk.junkmail.actors.SessionCleaner.Clean
import tk.junkmail.services.SessionService
import scala.concurrent._

import scala.util.{Failure, Success}

/**
 * @author  Alexey Ponkin
 * @version 1, 29 Aug 2014
 */
object SessionCleaner {
  sealed trait SessionCleanerMessage
  case object Clean extends SessionCleanerMessage

  def props(sessionService:SessionService) = Props(classOf[SessionCleaner], sessionService)

}

class SessionCleaner(sessionService:SessionService) extends Actor with ActorLogging{

  override def receive = {
    case Clean =>
      import context.dispatcher

      future {
        sessionService.deleteExpiredSessions()
      } onComplete {
        case Success(n) => log.debug("Delete {} expired sessions", n);
        case Failure(e) => log.error("Unable to delete expired sessions {}", e)
      }
  }
}
