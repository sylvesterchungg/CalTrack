package controller

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Label, ProgressBar, TextField, Alert}
import javafx.scene.chart.{XYChart, PieChart}
import java.net.URL
import java.time.LocalDate
import java.util.ResourceBundle
import services.{DailyDiary, GoalService}

class DashboardController extends Initializable {
  @FXML private var goalBar:   ProgressBar = null
  @FXML private var goalLabel: Label       = null
  @FXML private var macroPie:  PieChart    = null
  @FXML private var weekChart: javafx.scene.chart.BarChart[String, Number] = null
  @FXML private var goalField: TextField   = null

  private var diary: DailyDiary = null
  private var goals: GoalService = null

  def initWith(d: DailyDiary, g: GoalService): Unit = { diary = d; goals = g; refresh() }

  override def initialize(url: URL, rb: ResourceBundle): Unit = () // nothing yet

  @FXML def handleApplyGoal(): Unit = {
    val v = Option(goalField).flatMap(f => Option(f.getText)).map(_.trim).getOrElse("")
    try {
      if (v.nonEmpty) goals.setTarget(v.toDouble)
      refresh()
    } catch { case _: Throwable => warn("Enter a number like 2200") }
  }

  @FXML def refresh(): Unit = {
    if (diary == null || goals == null) return
    val (kc, p, c, f) = diary.totals(LocalDate.now())
    val goal = goals.getTarget

    // progress
    val used = kc.min(goal)
    if (goalBar != null) goalBar.setProgress(if (goal <= 0) 0 else used / goal)
    if (goalLabel != null) goalLabel.setText(f"Calories remaining: ${goal - kc}%.0f / ${goal}%.0f")

    // macros pie
    if (macroPie != null) {
      macroPie.getData.clear()
      macroPie.getData.add(new PieChart.Data("Protein", p))
      macroPie.getData.add(new PieChart.Data("Fats",    f))
      macroPie.getData.add(new PieChart.Data("Carbs",   c))
    }

    // simple last-7-days bar
    if (weekChart != null) {
      weekChart.getData.clear()
      val series = new XYChart.Series[String, Number]()
      diary.lastNDays(7).foreach { case (day, cal) =>
        series.getData.add(new XYChart.Data[String, Number](day.toString.drop(5), cal))
      }
      weekChart.getData.add(series)
    }
  }

  private def warn(m: String): Unit = {
    val a = new Alert(Alert.AlertType.WARNING); a.setHeaderText(null); a.setContentText(m); a.showAndWait()
  }
}
