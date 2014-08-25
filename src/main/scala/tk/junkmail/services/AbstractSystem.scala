package tk.junkmail.services

import akka.actor.ActorSystem

/**
 * @author  Alexey Ponkin
 * @version 1, 24 Aug 2014
 */
trait AbstractSystem {
  implicit def system: ActorSystem
}
