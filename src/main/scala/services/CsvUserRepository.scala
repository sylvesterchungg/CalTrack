package services

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.security.MessageDigest
import scala.jdk.CollectionConverters._
import scala.util.Try
import models.User

final class CsvUserRepository(baseDir: Path = CsvUserRepository.defaultBaseDir) extends UserRepository {

  private val file: Path = {
    if (!Files.exists(baseDir)) Files.createDirectories(baseDir)
    val f = baseDir.resolve("users.csv")
    if (!Files.exists(f)) {
      Files.write(
        f,
        "username,passwordHash,age,gender,weight,height\n".getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE
      )
    }
    f
  }
    println("[UserRepo] CSV path = " + file.toAbsolutePath.normalize.toString)

  private def sha256(s: String): String = {
    val md = MessageDigest.getInstance("SHA-256")
    md.digest(s.getBytes(StandardCharsets.UTF_8)).map("%02x".format(_)).mkString
  }

  def hashPassword(plain: String): String = sha256(plain)

  override def save(u: User): Either[String, Unit] = this.synchronized {
    val res =
    if (find(u.username).isDefined) Left("Username already exists.")
    else {
      val line = s"${u.username},${u.passwordHash},${u.age},${u.gender},${u.weight},${u.height}\n"
      Files.write(file, line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.CREATE)
      Right(())
    }
    println(s"[UserRepo] save(${u.username}) -> " + res)
    res
  }

  override def find(username: String): Option[User] = this.synchronized {
    Files.readAllLines(file, StandardCharsets.UTF_8).asScala.iterator.drop(1).flatMap { ln =>
      val p = ln.split(",", -1).map(_.trim)
      if (p.length >= 6 && p(0).equalsIgnoreCase(username))
        Try(User(p(0), p(1), p(2).toInt, p(3), p(4).toDouble, p(5).toDouble)).toOption
      else None
    }.toSeq.headOption
  }

  override def validate(username: String, plainPassword: String): Boolean =
    val ok = find(username).exists(u => u.passwordHash == sha256(plainPassword))
    println(s"[UserRepo] validate($username) -> $ok")
    ok
}

object CsvUserRepository {
  // Quick fix: write to a normal folder that always exists/writable
  val defaultBaseDir: Path =
    Paths.get(System.getProperty("user.dir"), "data") // -> <project>/data/users.csv
}
