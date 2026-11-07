package services

import models.User

trait UserRepository {
  def save(u: User): Either[String, Unit]
  def find(username: String): Option[User]
  def validate(username: String, plainPassword: String): Boolean
}
