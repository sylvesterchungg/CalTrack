package models

sealed trait Goal { def applyTo(tdee: Double): Double; val label: String }

case object Maintain extends Goal {
  val label = "Maintain"
  def applyTo(tdee: Double): Double = tdee
}

final case class Lose(percent: Int) extends Goal {
  val label = s"Cut ${percent}%"
  def applyTo(tdee: Double): Double = tdee * (1.0 - percent.max(0).min(50) / 100.0)
}

final case class Gain(percent: Int) extends Goal {
  val label = s"Bulk ${percent}%"
  def applyTo(tdee: Double): Double = tdee * (1.0 + percent.max(0).min(50) / 100.0)
}
