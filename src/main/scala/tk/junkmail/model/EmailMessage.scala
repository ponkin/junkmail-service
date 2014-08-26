package tk.junkmail.model

/**
 * @author  Alexey Ponkin
 * @version 1, 25 Aug 2014
 */
trait EmailMessage{
  def contentType : String
}

case class PlainEmail(override val contentType:String, body:String) extends EmailMessage

case class Attachment(override val contentType:String, body:String, name:String) extends EmailMessage

case class Envelope(from:String, subject:String, date:Long, message:PlainEmail, attachments:List[Attachment])


