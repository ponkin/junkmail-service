package tk.junkmail.actors

import java.io.FileInputStream
import java.util.Properties
import javax.mail.Session
import javax.mail.internet.MimeMessage

import org.specs2.mutable.Specification

import spray.json._
import DefaultJsonProtocol._

/**
 * @author  Alexey Ponkin
 * @version 1, 18 Aug 2014
 */
class MimeMessageUtilsTestSpec extends Specification {

  "The MimeUtils" should  {
    "get charset from content-type filed" in{
      MimeMessageUtils.charset(" text/plain; charset=UTF-8; boundary=\"Apple-Mail=_73E3F337-48DC-485A-B440-0216A99D8D1A\"") must equalTo("UTF-8")
    }

    "get mime type from content-type filed" in {
      MimeMessageUtils.contentType(" image/*; charset=UTF-8; boundary=\"Apple-Mail=_73E3F337-48DC-485A-B440-0216A99D8D1A\"") must equalTo("image/*")
    }

//    "get body from mime message" in {
//      MimeMessageUtils.body(new MimeMessage(Session.getDefaultInstance(new Properties()), new FileInputStream("src/test/resources/sportsru.eml"))).getOrElse(JsObject()).toJson.compactPrint must equalTo("NONE")
//    }

    "get name from content-type" in {
      MimeMessageUtils.name("image/jpg;\n\tx-unix-mode=0777;\n\tname=\"DSC_1497.jpg\"\n\t").getOrElse("NONE") must equalTo("DSC_1497.jpg")
    }

//    "get json with attachments" in {
//      MimeMessageUtils.attachments(
//        new MimeMessage(Session.getDefaultInstance(new Properties()),
//          new FileInputStream("src/test/resources/email.eml"))).toJson.compactPrint must equalTo("")
//    }
  }

}
