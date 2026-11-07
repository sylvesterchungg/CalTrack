package repository

import java.nio.file.{Files, Paths, StandardOpenOption}
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import scala.jdk.CollectionConverters._
import scala.util.Try
import models.User

object UserCsvRepository {
  private val dir  = Paths.get(System.getProperty("user.home"), ".caltrack")
  private val file = dir.resolve("users.csv")
  private val header = "username,passwordHash,age,gender,weight,height\n"

  private def ensureFile(): Unit = this.synchronized {
    if (!Files.exists(dir))  Files.createDirectories(dir)
    if (!Files.exists(file)) Files.write(file, header.getBytes(StandardCharsets.UTF_8))
  }

  private def sha256(s: String): String = {
    val md  = MessageDigest.getInstance("SHA-256")
    val dig = md.digest(s.getBytes(StandardCharsets.UTF_8))
    dig.map("%02x".format(_)).mkString
  }
  def hashPassword(plain: String): String = sha256(plain)

  def save(user: User): Either[String, Unit] = this.synchronized {
    ensureFile()
    if (find(user.username).isDefined) return Left("Username already exists.")
    val line = s"${user.username},${user.passwordHash},${user.age},${user.gender},${user.weight},${user.height}\n"
    Files.write(file, line.getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.APPEND, StandardOpenOption.CREATE)
    Right(())
  }

  def find(username: String): Option[User] = this.synchronized {
    ensureFile()
    val lines = Files.readAllLines(file, StandardCharsets.UTF_8).asScala.drop(1) // skip header
    lines.iterator
      .map(_.split(",", -1).map(_.trim))
      .find(p => p.length >= 6 && p(0) == username)
      .flatMap { p =>
        for {
          age    <- Try(p(2).toInt).toOption
          weight <- Try(p(4).toDouble).toOption
          height <- Try(p(5).toDouble).toOption
        } yield User(p(0), p(1), age, p(3), weight, height)
      }
  }

  /** Hash the incoming password, compare to stored hash. */
  def validate(username: String, plainPassword: String): Boolean =
    find(username).exists(u => u.passwordHash == hashPassword(plainPassword))
}
