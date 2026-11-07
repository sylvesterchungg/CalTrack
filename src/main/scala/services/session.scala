package services
import models.User

object Session {
  @volatile private var cur: Option[User] = None
  def currentUser: Option[User] = cur
  def setCurrentUser(u: User): Unit = { cur = Option(u) }
  def clear(): Unit = { cur = None }
}
