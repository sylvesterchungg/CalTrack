package repository

import models.Recipe
import scala.collection.mutable.ListBuffer

final class InMemoryRecipeRepository extends RecipeRepository {
  private val data = ListBuffer.empty[Recipe]

  def all(): List[Recipe] = data.toList

  def add(recipe: Recipe): Unit = {
    deleteByName(recipe.name) // replace if same name
    data += recipe
  }

  def findByName(name: String): Option[Recipe] =
    data.find(_.name.equalsIgnoreCase(name))

  def updateByName(name: String, updated: Recipe): Boolean = {
    val i = data.indexWhere(_.name.equalsIgnoreCase(name))
    if (i >= 0) { data.update(i, updated); true } else false
  }

  def deleteByName(name: String): Boolean = {
    val i = data.indexWhere(_.name.equalsIgnoreCase(name))
    if (i >= 0) { data.remove(i); true } else false
  }
}
