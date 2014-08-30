package tk.junkmail.model


import org.joda.time.DateTime

import scala.slick.lifted.Tag

import scala.slick.driver.H2Driver.simple._
import com.github.tototoshi.slick.H2JodaSupport._

/**
 * @author  Alexey Ponkin
 * @version 1, 24 Aug 2014
 */

class Sessions(tag: Tag) extends Table[(String, String, DateTime, DateTime)](tag, "SESSIONS") {
  def id = column[String]("SESSION_ID", O.PrimaryKey) // This is the primary key column
  def email = column[String]("EMAIL", O.NotNull)
  def createdAt = column[DateTime]("CREATED_AT", O.NotNull)
  def expiredAt = column[DateTime]("EXPIRED_AT", O.NotNull)
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, email, createdAt, expiredAt)
  def uniqueEmail = index("UNIQUE_EMAIL_IDX", email, unique = true)
}