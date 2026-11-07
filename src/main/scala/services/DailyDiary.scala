package services

import java.time.LocalDate
import scala.collection.mutable
final case class Intake(name: String, kc: Double, p: Double, c: Double, f: Double, day: LocalDate)

class DailyDiary {
  private val entries = mutable.ArrayBuffer.empty[Intake]

  def add(name: String, kc: Double, p: Double, c: Double, f: Double, day: LocalDate = LocalDate.now()): Unit =
    entries += Intake(name, kc, p, c, f, day)

  def totals(day: LocalDate): (Double, Double, Double, Double) =
    entries.view.filter(_.day == day).foldLeft((0.0,0.0,0.0,0.0)) { case ((ak,ap,ac,af), x) =>
      (ak + x.kc, ap + x.p, ac + x.c, af + x.f)
    }

  /** For a simple “last 7 days” bar chart (optional) */
  def lastNDays(n: Int): Seq[(LocalDate, Double)] = {
    val today = LocalDate.now
    (0 until n).reverse.map(d => {
      val day = today.minusDays(d.toLong)
      day -> totals(day)._1
    })
  }
}


