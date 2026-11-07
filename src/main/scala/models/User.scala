package models

final case class User(
  username: String,
  passwordHash: String,
  age: Int,
  gender: String,
  weight: Double,
  height: Double
)
