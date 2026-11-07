
package models

case class Food(
  override val name: String,
  override val calories: Double,
  override val protein: Double,
  override val carbs: Double,
  override val fat: Double
) extends Item(name, calories, protein, carbs, fat)
