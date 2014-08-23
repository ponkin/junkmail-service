package tk.junkmail.actors

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.ByteBuffer
import java.util.Properties
import javax.mail.Session
import javax.mail.internet.{MimeBodyPart, MimeMultipart, MimeMessage}

import akka.actor._
import akka.util.ByteString
import com.typesafe.scalalogging.slf4j.Logger
import org.eclipse.jetty.websocket.common.WebSocketRemoteEndpoint
import org.slf4j.LoggerFactory
import tk.junkmail.actors.Mailbox.{Ping, Unsubscribe, Message}

import spray.json._

import org.apache.commons.codec.binary.Base64

import scala.annotation.tailrec


/**
 * @author  Alexey Ponkin
 * @version 1, 13 Aug 2014
 */
object Mailbox{
  sealed trait MailboxMessage
  case class Message(message: ByteString) extends MailboxMessage
  case class Unsubscribe() extends MailboxMessage
  case class Ping() extends MailboxMessage

  def props(client:WebSocketRemoteEndpoint): Props = Props(classOf[Mailbox], client)

}

object MimeMessageUtils {

  val log = Logger(LoggerFactory.getLogger("MimeMessageUtils"))


  val ch = """charset=([\w\-]+?)[\n;\s]{1}""".r

  private[actors]  def charset(contentType:String) = ch findFirstMatchIn contentType match {
    case Some(ch(charset)) => charset
    case None => ""
  }

  val ct = """([\w\-]+?)[/]{1}([\w\-\*]+?)[\n;\s]{1}""".r

  private[actors] def contentType(contentType:String) = ct findFirstMatchIn contentType match {
    case Some(ct(major,minor)) => major + "/" + minor
    case None => ""
  }


  private[actors] def body(message : MimeMessage) = {

    @tailrec def inner(part : MimeMultipart):Option[JsObject] = {
      part.getBodyPart(0) match {
        case x : MimeBodyPart => x.getContent match {
          case y : MimeMultipart => inner(y)
          case is : InputStream => Some(
            JsObject(
              "type" -> JsString(contentType(x.getContentType)),
              "body" -> JsString(Base64.encodeBase64String(Stream.continually(is.read).takeWhile(-1 !=).map(_.toByte).toArray)))
            )
          case s : String => Some(
            JsObject(
              "type" -> JsString(contentType(x.getContentType)),
              "body" -> JsString(Base64.encodeBase64String(s.getBytes)))
          )
        }
        case _ => None
      }
    }
      message.getContent match {
        case mm : MimeMultipart => inner(mm)
        case is : InputStream => Some(
          JsObject(
            "type" -> JsString(message.getContentType),
            "body" -> JsString(Base64.encodeBase64String(Stream.continually(is.read).takeWhile(-1 !=).map(_.toByte).toArray)))
        )
        case s : String => Some(
          JsObject(
            "type" -> JsString(message.getContentType),
            "body" -> JsString(Base64.encodeBase64String(s.getBytes)))
        )
      }

  }

  val n = """name=[\"\']??([\w,\s-\.]+?)[\"\']??[\n;\s]{1}""".r

  private[actors] def name( contentType:String ) = n findFirstMatchIn contentType match {
    case Some(n(filename)) => Some(filename)
    case None => None
  }

  private[actors] def attachments(message : MimeMessage):List[JsObject] = {

    def inner(  part : MimeBodyPart):List[JsObject] = {
      if(part.isMimeType("multipart/*")) {
        val mm = part.getContent.asInstanceOf[MimeMultipart]
        List.range(0, mm.getCount) flatMap {
          x => inner(mm.getBodyPart(x).asInstanceOf[MimeBodyPart])
        }
      }else if(part.getFileName != null )
        part.getContent match {
          case is : InputStream =>
            List(JsObject(
              "name" -> JsString(part.getFileName),
              "type" -> JsString(contentType(part.getContentType)),
              "body" -> JsString(Base64.encodeBase64String(Stream.continually(is.read).takeWhile(-1 !=).map(_.toByte).toArray))
            ) )
          case s : String =>
            List(JsObject(
              "name" -> JsString(part.getFileName),
              "type" -> JsString(contentType(part.getContentType)),
              "body" -> JsString(Base64.encodeBase64String(s.getBytes))
            ) )
          case mmm : MimeMultipart =>
            List.range(0, mmm.getCount) flatMap {
              x => inner(mmm.getBodyPart(x).asInstanceOf[MimeBodyPart])
            }
        }
      else List.empty
    }

    if(message.isMimeType("multipart/*")){
      val pp = message.getContent.asInstanceOf[MimeMultipart]
      List.range(0, pp.getCount) flatMap {
        x => inner(pp.getBodyPart(x).asInstanceOf[MimeBodyPart])
      }
    }else if(message.getFileName != null)
      message.getContent match {
        case is : InputStream =>
          List(JsObject(
            "name" -> JsString(message.getFileName),
            "type" -> JsString(contentType(message.getContentType)),
            "body" -> JsString(Base64.encodeBase64String(Stream.continually(is.read).takeWhile(-1 !=).map(_.toByte).toArray))
          ) )
        case s : String =>
          List(JsObject(
            "name" -> JsString(message.getFileName),
            "type" -> JsString(contentType(message.getContentType)),
            "body" -> JsString(Base64.encodeBase64String(s.getBytes))
          ) )
        case mmm : MimeMultipart =>
          List.range(0, mmm.getCount) flatMap {
            x => inner(mmm.getBodyPart(x).asInstanceOf[MimeBodyPart])
          }
      }
    else List.empty

  }
  import DefaultJsonProtocol._

  implicit def MimeMessageToString(message : MimeMessage) =
    JsObject(
      "from" -> JsString(message.getFrom.headOption.getOrElse("").toString),
      "subject" -> JsString(message.getSubject),
      "date" -> JsNumber(message.getSentDate.getTime),
      "message" -> body(message).getOrElse(JsObject()).toJson,
      "attachments" -> attachments(message).toJson
    ).prettyPrint

}

class Mailbox(val client: WebSocketRemoteEndpoint) extends Actor with ActorLogging{

  import scala.concurrent.duration._
  import MimeMessageUtils._

  implicit val executor = context.dispatcher
  val pinger = context.system.scheduler.schedule(10 seconds, 30 seconds, self, Ping)
  val s = Session.getDefaultInstance(new Properties())

  override def receive = {
    case Message(message) =>
      client.sendString(new MimeMessage(s, new ByteArrayInputStream(message.toArray)))
    case Unsubscribe =>
      pinger.cancel()
      context stop self
    case Ping =>
      client.sendPing(ByteBuffer.wrap("PING".getBytes))
    case x =>
      log.debug("Unknown message "+x)

  }

}
