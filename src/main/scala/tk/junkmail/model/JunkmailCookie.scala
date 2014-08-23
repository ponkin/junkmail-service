package tk.junkmail.model

import javax.servlet.http.Cookie

/**
 * @author  Alexey Ponkin
 * @version 1, 13 Aug 2014
 */
case class JunkmailCookie(id : String, maxAge : Int = -1, path : String = "/messages"){
  def asCookie = {
    val c = new Cookie("id", id)
    c.setMaxAge(Int.MaxValue)
    c.setPath(path)
    c
  }
}
