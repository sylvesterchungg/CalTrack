package models

case class Recipe(
  override val name: String,
  override val calories: Double,
  override val protein: Double,
  override val carbs: Double,
  override val fat: Double,
  
  ingredients: List[Food]
) extends Item(name, calories, protein, carbs, fat)