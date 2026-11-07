package models

sealed trait ActivityLevel { val factor: Double; val label: String }

object ActivityLevel {
  case object Sedentary    extends ActivityLevel { val factor = 1.20; val label = "Sedentary (little/no exercise)" }
  case object Light        extends ActivityLevel { val factor = 1.375; val label = "Light (1–3 days/week)" }
  case object Moderate     extends ActivityLevel { val factor = 1.55; val label = "Moderate (3–5 days/week)" }
  case object Active       extends ActivityLevel { val factor = 1.725; val label = "Active (6–7 days/week)" }
  case object VeryActive   extends ActivityLevel { val factor = 1.90; val label = "Very active (hard daily)" }

  val all: List[ActivityLevel] = List(Sedentary, Light, Moderate, Active, VeryActive)

  /** Best-effort parser (defaults to Sedentary). */
  def fromString(s: String): ActivityLevel = Option(s).map(_.toLowerCase.trim) match {
    case Some("light") | Some("lightly active")                 => Light
    case Some("moderate") | Some("moderately active")           => Moderate
    case Some("active")                                         => Active
    case Some("very active") | Some("veryactive") | Some("hard")=> VeryActive
    case _                                                      => Sedentary
  }
}
