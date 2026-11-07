package repository

import models.Food

trait FoodRepository {
  def all(): List[Food]
  def add(food: Food): Unit
  def findByName(name: String): Option[Food]
  def updateByName(oldname: String, updated: Food): Boolean
  def deleteByName(name: String): Boolean
}
