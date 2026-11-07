package services

final class GoalService {
  @volatile private var dailyTargetKcal: Double = 2000.0

  /** Set daily target safely (bounds avoid weird UI %) */
  def setTarget(kcal: Double): Unit =
    dailyTargetKcal = kcal.max(800).min(8000)

  def getTarget: Double = dailyTargetKcal

  /** 0..1 progress for dashboards */
  def progress(consumedKcal: Double): Double =
    if (dailyTargetKcal <= 0) 0.0
    else (consumedKcal / dailyTargetKcal).max(0.0).min(1.0)
}
