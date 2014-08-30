package tk.junkmail.services

import tk.junkmail.model.Sessions

import scala.slick.driver.H2Driver.simple._

import com.github.nscala_time.time.Imports._
import com.github.tototoshi.slick.H2JodaSupport._


/**
 * @author  Alexey Ponkin
 * @version 1, 24 Aug 2014
 */
object SessionService {

  def apply() = new SessionService() with DataSource {

    val DB = Database.forURL("jdbc:h2:mem:junkmail;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    override implicit def session:Session = DB.createSession()

    sessions.ddl.create

  }

}

trait SessionService {

  this: DataSource =>

  private val sessions = TableQuery[Sessions]

  def find(id:String) = sessions.filter( _.id === id).firstOption

  def delete(id:String) = sessions.filter(_.id === id).delete

  def create(id:String, email:String, createdAt:DateTime=DateTime.now, expiredAt:DateTime=DateTime.now + 1.day) = sessions += (id, email, createdAt, expiredAt)

  def prolongSession(id:String) = {
    val q = for { s <- sessions if s.id === id } yield s.expiredAt
    q.update(DateTime.now + 1.day)
  }

  def deleteExpiredSessions(maxAge:DateTime=DateTime.now) = sessions.filter(_.expiredAt < maxAge).delete

}
