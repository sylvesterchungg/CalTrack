// src/main/scala/repository/CsvFoodRepository.scala
package repository

import models.Food
import java.nio.file.{Files, Path, StandardOpenOption}
import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters._
import scala.util.Try

final class CsvFoodRepository(val csv: Path) extends FoodRepository {
  private val header = "name,calories,protein,carbs,fat\n"
  @volatile private var items: Vector[Food] = Vector.empty

  load()

  private def ensureFile(): Unit = this.synchronized {
    val parent = csv.getParent
    if (parent != null && !Files.exists(parent)) Files.createDirectories(parent)
    if (!Files.exists(csv)) Files.write(csv, header.getBytes(StandardCharsets.UTF_8))
  }

  private def load(): Unit = this.synchronized {
    ensureFile()
    val lines = Files.readAllLines(csv, StandardCharsets.UTF_8).asScala.drop(1) // skip header
    items = lines.flatMap { ln =>
      val p = ln.split(",", -1).map(_.trim)
      if (p.length >= 5)
        Try(Food(p(0), p(1).toDouble, p(2).toDouble, p(3).toDouble, p(4).toDouble)).toOption
      else None
    }.toVector
  }

  private def saveAll(): Unit = this.synchronized {
    val body = items
      .map(f => s"${f.name},${f.calories},${f.protein},${f.carbs},${f.fat}\n")
      .mkString
    Files.write(
      csv,
      (header + body).getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.TRUNCATE_EXISTING,
      StandardOpenOption.CREATE
    )
  }

  // ---------- FoodRepository ----------
  override def all(): List[Food] = this.synchronized { items.toList }

  override def add(f: Food): Unit = this.synchronized {
    
    val had = items.indexWhere(_.name == f.name)
    if (had >= 0) items = items.updated(had, f)
    else          items = items :+ f
    saveAll()
  }

  override def deleteByName(name: String): Boolean = this.synchronized {
    val before = items.size
    items = items.filterNot(_.name == name)
    val changed = items.size != before
    if (changed) saveAll()
    changed
  }

  
  override def updateByName(oldname: String, updated: Food): Boolean = this.synchronized {
    val idx = items.indexWhere(_.name == oldname)
    val changed = idx >= 0
    if (changed) {
      items = items.updated(idx, updated)
      saveAll()
    }
    changed
  }

  override def findByName(name: String): Option[Food] = this.synchronized {
    items.find(_.name == name)
  }
}
