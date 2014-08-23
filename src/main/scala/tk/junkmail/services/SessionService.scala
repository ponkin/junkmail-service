package tk.junkmail.services

import com.typesafe.scalalogging.slf4j.Logger
import org.apache.commons.lang3.RandomStringUtils
import org.h2.jdbc.JdbcSQLException
import org.slf4j.LoggerFactory

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.util.{Failure, Success, Try}

/**
 * @author  Alexey Ponkin
 * @version 1, 13 Aug 2014
 */

object SessionService{
  def apply() = new SessionService()
}
class SessionService {


  val log = Logger(LoggerFactory.getLogger(classOf[SessionService]))

  val sessions = TableQuery[Sessions]
  val db = Database.forURL("jdbc:h2:mem:junkmail;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

  db.withSession { session =>
    sessions.ddl.create(session)
  }

  def create(id:String) = {
    db.withSession { implicit session =>
      calc_inboxName(6) { inboxName =>
        sessions.map(s => (s.id, s.inbox))
          .insert((id,inboxName))
        (id,inboxName)
      }
    }
  }

  def delete(id:String) = {
    db.withSession { implicit session =>
      sessions.filter(s => s.id === id)
        .delete
    }
  }

  def findById(id:String) = {
    db.withSession { implicit session =>
      sessions.filter(s => s.id === id).firstOption
    }
  }

  /**
   * We are incrementing random inbox name each time when insert generates exception
   * @param length
   * @param f
   * @tparam T
   * @return
   */
  def calc_inboxName[T](length: Int)(f: String => T): T = {
    Try(f(RandomStringUtils.randomAlphanumeric(length)+"@junkmail.tk")) match {
        case Success(v) => v
        case Failure(e) =>
          if (length < 8){
            calc_inboxName(length+1)(f)
          }else{
            throw e
          }

    }


  }


}

// Definition of the SUPPLIERS table
class Sessions(tag: Tag) extends Table[(String, String)](tag, "SESSIONS") {
  def id = column[String]("ID", O.PrimaryKey) // This is the primary key column
  def inbox = column[String]("INBOX", O.NotNull)
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, inbox)
  def idx = index("idx_inboxName", (inbox), unique = true)
}
