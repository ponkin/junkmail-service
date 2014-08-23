package tk.junkmail.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging.InfoLevel
import spray.http._
import spray.http.StatusCodes.{ NotFound }
import spray.routing.{HttpService, Route, Directives, RouteConcatenation}
import spray.routing.directives.LogEntry

/**
 * @author  Alexey Ponkin
 * @version 1, 12 Aug 2014
 */
trait MainActors {
  this: AbstractSystem =>

  lazy val dispatcher = system.actorOf(MessageDispatcher.props, "messages")
  lazy val socketService = system.actorOf(SocketService.props(dispatcher), "socket")

}

trait JunkmailApi extends RouteConcatenation with StaticRoute with AbstractSystem {
  this: MainActors =>

  lazy val staticService =  system.actorOf(Props(classOf[StaticService], routes))

  lazy val routes = logRequest(showReq _) {
      staticRoute
  }
  private def showReq(req : HttpRequest) = LogEntry(req.uri, InfoLevel)
}

trait AbstractSystem {
  implicit def system: ActorSystem
}

class StaticService(route : Route) extends Actor with HttpService with ActorLogging {
  implicit def actorRefFactory = context
  def receive = runRoute(route)
}


trait StaticRoute extends Directives {
  this: AbstractSystem =>

  import MediaTypes._

  lazy val staticRoute =
    path("favicon.ico") {
      getFromResource("favicon.ico")
    } ~
      pathPrefix("js") {
        respondWithMediaType(`application/javascript`){
          getFromResourceDirectory("public/js/")
        }
      } ~
      pathPrefix("css") {
        respondWithMediaType(`text/css`){
          getFromResourceDirectory("public/css/")
        }

      } ~
      pathPrefix("fonts") {
          getFromResourceDirectory("public/fonts/")
      } ~
      pathPrefix("partials") {
        respondWithMediaType(`text/html`){
          getFromResourceDirectory("public/partials/")
        }
      } ~
      pathEndOrSingleSlash {
        setCookie(HttpCookie("id", content = java.util.UUID.randomUUID().toString)){
          getFromResource("public/index.html")
        }
      } ~ complete(NotFound)
}