package repository

import models.Recipe

trait RecipeRepository {
  def all(): List[Recipe]
  def add(recipe: Recipe): Unit
  def findByName(name: String): Option[Recipe]
  def updateByName(name: String, updated: Recipe): Boolean
  def deleteByName(name: String): Boolean
}
