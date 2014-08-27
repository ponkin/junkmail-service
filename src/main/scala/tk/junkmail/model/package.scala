package tk.junkmail

import java.io.InputStream
import javax.mail.internet.{MimeBodyPart, MimeMessage, MimeMultipart}

import org.apache.commons.codec.binary.Base64
import spray.json.{DefaultJsonProtocol, _}

/**
 * @author  Alexey Ponkin
 * @version 1, 25 Aug 2014
 */
package object model {

  private implicit def InputStreamToArray(is: InputStream) = Stream.continually(is.read).takeWhile(-1 !=).map(_.toByte).toArray


  object ModelJsonProtocol extends DefaultJsonProtocol {

    implicit val initMailboxFormat = jsonFormat1(InitMailbox)

    implicit val plainEmailFormat = jsonFormat2(PlainEmail)
    
    implicit val attachmentFormat = jsonFormat3(Attachment)
    
    implicit val envelopeFormat = jsonFormat5(Envelope)

  }

  object ModelStringProtocol {

    import tk.junkmail.model.ModelJsonProtocol._

    implicit def InitMailbox2String(value : InitMailbox) = value.toJson.compactPrint

    implicit def PlainEmail2String(value:PlainEmail) = value.toJson.compactPrint
    
    implicit def Envelope2String(value:Envelope) = value.toJson.compactPrint

  }

  def body(message:MimeMessage) = {
        message.getContent match {
          case part: MimeMultipart => fetchBody(part)
          case s:String => Some(PlainEmail(message.getContentType, Base64.encodeBase64String(s.getBytes)))
          case i:InputStream => Some(PlainEmail(message.getContentType, Base64.encodeBase64String(i)))
        }
  }

  private[model] def fetchBody(part:MimeMultipart):Option[PlainEmail] = {
      part.getBodyPart(0) match {
        case m: MimeBodyPart => m.getContent match {
          case mm:MimeMultipart => fetchBody(mm)
          case s:String => Some(PlainEmail(m.getContentType, Base64.encodeBase64String(s.getBytes)))
          case i:InputStream => Some(PlainEmail(m.getContentType, Base64.encodeBase64String(i)))
        }
        case _ => None
      }

  }

  def fetchAttachments(message:MimeMessage):List[Attachment] = {

    def parts(part:MimeMultipart) = {
      for {
        k <- (0 until part.getCount).toList
        p = part.getBodyPart(k).asInstanceOf[MimeBodyPart]
      } yield p
    }

    def inner(part:MimeMultipart):List[Attachment] = {
      for {
        p <- parts(part)
      } yield p.getContent match {
        case mm:MimeMultipart => inner(mm)
        case s:String if p.getFileName != null => List(Attachment(p.getContentType, Base64.encodeBase64String(s.getBytes), p.getFileName))
        case i:InputStream if p.getFileName != null => List(Attachment(p.getContentType, Base64.encodeBase64String(i), p.getFileName))
        case _ => List.empty
      }
    }.flatten

    message.getContent match {
      case mm:MimeMultipart => inner(mm)
      case _ => List.empty
    }

  }



}
