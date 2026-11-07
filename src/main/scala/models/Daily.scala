package models

import java.time.LocalDate
import scala.collection.mutable.ListBuffer

/** One diary row (what a user ate on a date). */
final case class DiaryEntry(
  user: User,
  date: LocalDate,
  name: String,
  calories: Double,
  protein: Double,
  carbs: Double,
  fat: Double
)

/** Simple in-memory diary per user. */
final class DailyLog {
  private val entries = ListBuffer.empty[DiaryEntry]
  private var dailyGoalCalories: Double = 2000.0

  // getters/setters
  def setGoal(kcal: Double): Unit = if (kcal > 0) dailyGoalCalories = kcal
  def getGoal: Double = dailyGoalCalories

  // commands
  def add(user: User, date: LocalDate, item: Item, qty: Double = 1.0): Unit = {
    val q = if (qty <= 0) 1.0 else qty
    entries += DiaryEntry(
      user, date, item.name,
      item.calories*q, item.protein*q, item.carbs*q, item.fat*q
    )
  }

  def removeAt(user: User, date: LocalDate, visibleIndex: Int): Boolean = {
    val todays = entries.zipWithIndex.filter{ case (e, _) => e.user == user && e.date == date }
    if (visibleIndex >= 0 && visibleIndex < todays.size) {
      entries.remove(todays(visibleIndex)._2); true
    } else false
  }

  // queries
  def list(user: User, date: LocalDate): List[DiaryEntry] =
    entries.filter(e => e.user == user && e.date == date).toList

  def totals(user: User, date: LocalDate): (Double, Double, Double, Double) = {
    val d = list(user, date)
    (d.map(_.calories).sum, d.map(_.protein).sum, d.map(_.carbs).sum, d.map(_.fat).sum)
  }
}
