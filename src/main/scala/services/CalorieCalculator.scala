package services

import models._

object CalorieCalculator {

  /** BMI = kg / (m^2) */
  def bmi(heightCm: Double, weightKg: Double): Double = {
    val h = heightCm / 100.0
    if (h <= 0) 0.0 else weightKg / (h * h)
  }

  /** Mifflin–St Jeor BMR (kcal/day). */
  def bmrMifflin(sex: Sex, weightKg: Double, heightCm: Double, ageYears: Int): Double =
    10.0 * weightKg + 6.25 * heightCm - 5.0 * ageYears + sex.bmrBias

  /** TDEE = BMR * activity factor. */
  def tdee(bmr: Double, activity: ActivityLevel): Double = bmr * activity.factor

  /** One-call daily target calories from personal data + activity + goal. */
  def dailyCalories(sex: Sex, ageYears: Int, heightCm: Double, weightKg: Double,
                    activity: ActivityLevel, goal: Goal): Double = {
    val base = bmrMifflin(sex, weightKg, heightCm, ageYears)
    val t    = tdee(base, activity)
    goal.applyTo(t)
  }

  /** Macro targets by percentage split (must sum ~100%). Returns grams (P,C,F). */
  def macroByPercent(calories: Double, proteinPct: Double, carbsPct: Double, fatPct: Double): (Double, Double, Double) = {
    val total = proteinPct + carbsPct + fatPct
    val (p, c, f) = if (total <= 0) (0.0, 0.0, 0.0) else {
      (proteinPct / total, carbsPct / total, fatPct / total)
    }
    val pGrams = calories * p / 4.0
    val cGrams = calories * c / 4.0
    val fGrams = calories * f / 9.0
    (pGrams, cGrams, fGrams)
  }

  /**
    * Macro targets with protein by bodyweight. e.g., proteinGPerKg=1.6,
    * remaining calories split by carbs/fat percentages (they don't need to sum 100).
    * Returns grams (P,C,F).
    */
  def macroByProteinPerKg(calories: Double, weightKg: Double, proteinGPerKg: Double,
                          carbsShare: Double, fatShare: Double): (Double, Double, Double) = {
    val pGrams = (weightKg max 0.0) * (proteinGPerKg max 0.0)
    val pCals  = pGrams * 4.0
    val rem    = (calories - pCals) max 0.0
    val totalS = (carbsShare max 0.0) + (fatShare max 0.0)
    val (cPart, fPart) = if (totalS <= 0) (0.0, 0.0) else (carbsShare/totalS, fatShare/totalS)
    val cGrams = rem * cPart / 4.0
    val fGrams = rem * fPart / 9.0
    (pGrams, cGrams, fGrams)
  }

  /** Sum nutrition over any mix of Foods/Recipes (polymorphic on Item). */
  def totals(items: Seq[Item]): (Double, Double, Double, Double) = {
    val cal = items.map(_.calories).sum
    val pro = items.map(_.protein).sum
    val car = items.map(_.carbs).sum
    val fat = items.map(_.fat).sum
    (cal, pro, car, fat)
  }
}
