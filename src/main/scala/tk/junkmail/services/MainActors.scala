package tk.junkmail.services

import tk.junkmail.actors.{EmailEndpoint, MessageDispatcher}

/**
 * @author  Alexey Ponkin
 * @version 1, 24 Aug 2014
 */
trait MainActors {
  this: AbstractSystem =>

  val dispatcher = system.actorOf(MessageDispatcher.props)
  val emailEndpoint = system.actorOf(EmailEndpoint.props(dispatcher))

}
