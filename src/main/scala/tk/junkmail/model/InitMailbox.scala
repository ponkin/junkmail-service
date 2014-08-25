package tk.junkmail.model

import spray.json._
import spray.json.DefaultJsonProtocol

/**
 * @author  Alexey Ponkin
 * @version 1, 25 Aug 2014
 */
case class InitMailbox(inboxName:String)

object ModelJsonProtocol extends DefaultJsonProtocol {

  implicit val initMailboxFormat = jsonFormat1(InitMailbox)

}

object ModelStringProtocol {

  import ModelJsonProtocol._

  implicit def InitMailbox2String(value : InitMailbox) = value.toJson.compactPrint
}
