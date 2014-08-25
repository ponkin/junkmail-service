package tk.junkmail.model


import java.sql.Date

import scala.slick.lifted.Tag

import scala.slick.driver.H2Driver.simple._

/**
 * @author  Alexey Ponkin
 * @version 1, 24 Aug 2014
 */

class Sessions(tag: Tag) extends Table[(String, String, Date)](tag, "SESSIONS") {
  def id = column[String]("SESSION_ID", O.PrimaryKey) // This is the primary key column
  def email = column[String]("EMAIL")
  def createdAt = column[Date]("CREATED_AT")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, email, createdAt)
  def uniqueEmail = index("UNIQUE_EMAIL_IDX", email, unique = true)
}