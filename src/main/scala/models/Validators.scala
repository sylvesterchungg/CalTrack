package models

object Validators {
  private def parseDoubleOpt(s: String): Option[Double] =
    Option(s).map(_.trim).filter(_.nonEmpty).flatMap(x => scala.util.Try(x.toDouble).toOption)

  def nonEmpty(s: String): Boolean =
    s != null && s.trim.nonEmpty

  /** Validate and build a FoodItem in one go. */
  def validateFoodInputs(
    name: String, cal: String, protein: String, carbs: String, fat: String
  ): Either[String, Food] = {
    if (!nonEmpty(name)) Left("Name cannot be empty.")
    else {
      (parseDoubleOpt(cal), parseDoubleOpt(protein), parseDoubleOpt(carbs), parseDoubleOpt(fat)) match {
        case (Some(c), Some(p), Some(cb), Some(f))
          if c >= 0 && p >= 0 && cb >= 0 && f >= 0 =>
            Right(Food(name.trim, c, p, cb, f))
        case _ =>
            Left("Calories/Protein/Carbs/Fat must be valid non-negative numbers.")
      }
    }
  }
}
