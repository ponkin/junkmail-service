package tk.junkmail.services

import java.sql.Date
import java.util.Calendar

import tk.junkmail.model.Sessions

import scala.slick.driver.H2Driver.simple._


/**
 * @author  Alexey Ponkin
 * @version 1, 24 Aug 2014
 */
object SessionService {

  def apply() = new SessionService() with DataSource {

    override implicit def db = Database.forURL("jdbc:h2:mem:junkmail;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    override implicit def session = db.createSession()

    sessions.ddl.create

  }

}

trait SessionService {

  this: DataSource =>

  private val sessions = TableQuery[Sessions]

  implicit def session: Session

  def find(id:String) = sessions.filter( _.id === id).firstOption

  def delete(id:String) = sessions.filter(_.id === id).delete

  def create(id:String, email:String) = sessions += (id, email, new Date(Calendar.getInstance().getTime.getTime))

}
