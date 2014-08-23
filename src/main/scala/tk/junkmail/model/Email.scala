package tk.junkmail.model

import java.util.Date

/**
 * @author  Alexey Ponkin
 * @version 1, 14 Aug 2014
 */

case class Email(from: Array[String], subject: String, date: Date, charset: String, body: EmailBody.Body)


object EmailBody {

  sealed trait Body

  case class PlainText(body: String) extends Body

  case class Multipart(body: String) extends Body


}
