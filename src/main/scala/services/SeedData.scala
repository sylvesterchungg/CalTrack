package services

import models.Food
import scala.io.Source
import scala.util.Try

object SeedData {

  /** Load foods from resource file like "/db/food_data.txt".
    * Supports CSV or TSV; ignores blank lines and lines starting with '#'.
    * Header is optional (recommended: name,calories,protein,carbs,fat).
    */
  def loadFoodsFromResource(path: String): List[Food] = {
    val p = if (path.startsWith("/")) path else "/" + path
    val is = Option(getClass.getResourceAsStream(p))
      .getOrElse { println(s"[SeedData] resource NOT FOUND: $p"); return Nil }

    val src   = Source.fromInputStream(is, "UTF-8")
    val lines = try src.getLines().toList finally src.close()

    def parseD(s: String): Option[Double] =
      Try(s.trim.replace(',', '.').toDouble).toOption

    lines.iterator
      .map(_.trim)
      .filter(l => l.nonEmpty && !l.startsWith("#"))
      .filterNot(_.toLowerCase.startsWith("name"))      // drop header if present
      .flatMap { l =>
        val parts = l.split("[,\t]", -1).map(_.trim)
        if (parts.length >= 5) {
          for {
            cal <- parseD(parts(1))
            pro <- parseD(parts(2))
            car <- parseD(parts(3))
            fat <- parseD(parts(4))
          } yield Food(parts(0), cal, pro, car, fat)
        } else None
      }
      .toList
  }
}
