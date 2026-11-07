package models

sealed trait Sex { val name: String; val bmrBias: Double }
case object Male   extends Sex { val name = "male";   val bmrBias =  5.0  }
case object Female extends Sex { val name = "female"; val bmrBias = -161.0 }
/** If unknown, we use a neutral bias (average of male/female). */
case object Other  extends Sex { val name = "other";  val bmrBias = (5.0 - 161.0) / 2.0 }
