package tk.junkmail.model

import java.util.Properties
import javax.mail.Session
import javax.mail.internet.MimeMessage

import org.specs2.mutable._

/**
 * @author  Alexey Ponkin
 * @version 1, 25 Aug 2014
 */
object ParserSpec extends Specification{

    "Parser " should {
      "fetch body" in {
        body(new MimeMessage(Session.getDefaultInstance(new Properties()), getClass.getResourceAsStream("/email.eml"))) match {
          case Some(e) => e.contentType mustEqual "text/plain; charset=\"koi8-r\""
        }
      }
      "fetch attachments" in {

        fetchAttachments(new MimeMessage(Session.getDefaultInstance(new Properties()), getClass.getResourceAsStream("/email-with-attachments.eml"))).size mustEqual 1
      }
    }
}
