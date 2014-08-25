package tk.junkmail.services

import scala.slick.driver.H2Driver.simple._

/**
 * @author  Alexey Ponkin
 * @version 1, 24 Aug 2014
 */
trait DataSource {
  implicit def db: Database
}
