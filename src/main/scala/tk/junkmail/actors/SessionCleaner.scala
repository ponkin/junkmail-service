package tk.junkmail.actors

import akka.actor.{Props, ActorLogging, Actor}
import tk.junkmail.actors.SessionCleaner.Clean
import tk.junkmail.services.SessionService

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
      val n = sessionService.deleteExpiredSessions()
      log.info("Delete {} sessions.", n)
  }
}
