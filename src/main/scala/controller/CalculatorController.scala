package controller

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Alert, ChoiceBox, Label, TextField}
import java.net.URL
import java.util.ResourceBundle
import scala.util.boundary, boundary.break
import scala.util.Try
import services.Session
import models.User  

class CalculatorController extends Initializable {
  @FXML private var ageField:       TextField = null
  @FXML private var weightField:    TextField = null
  @FXML private var heightField:    TextField = null
  @FXML private var sexChoice:      ChoiceBox[String] = null
  @FXML private var activityChoice: ChoiceBox[String] = null
  @FXML private var goalChoice:     ChoiceBox[String] = null

  @FXML private var bmrLabel:    Label = null
  @FXML private var tdeeLabel:   Label = null
  @FXML private var targetLabel: Label = null

  override def initialize(url: URL, rb: ResourceBundle): Unit = {
    if (sexChoice != null) {
      sexChoice.getItems.setAll("Male", "Female")
      sexChoice.getSelectionModel.selectFirst()
    }
    if (activityChoice != null) {
      activityChoice.getItems.setAll(
        "Sedentary (x1.2)",
        "Lightly active (x1.375)",
        "Moderately active (x1.55)",
        "Very active (x1.725)",
        "Extra active (x1.9)"
      )
      activityChoice.getSelectionModel.select(0)
    }
    if (goalChoice != null) {
      goalChoice.getItems.setAll(
        "Lose weight (-20%)",
        "Maintain (0%)",
        "Gain weight (+15%)"
      )
      goalChoice.getSelectionModel.select(1)
    }

    prefillFromSession()
  }

  private def prefillFromSession(): Unit =
    Session.currentUser.foreach(prefillFrom)
  
  def prefillFrom(u: User): Unit = {
    setText(ageField,    u.age.toString)
    setText(weightField, format1(u.weight))
    setText(heightField, format1(u.height))
    if (sexChoice != null) {
      if (Option(u.gender).exists(_.equalsIgnoreCase("male"))) sexChoice.getSelectionModel.select("Male")
      else sexChoice.getSelectionModel.select("Female")
    }
  }

  @FXML def handleCalculate(): Unit = boundary {
    val age = parseInt(ageField).getOrElse {
      warn("Enter a valid age")
      break(())
    }
    val w = parseDouble(weightField).getOrElse {
      warn("Enter a valid weight (kg)")
      break(())
    }
    val h = parseDouble(heightField).getOrElse {
      warn("Enter a valid height (cm)")
      break(())
    }
    if (age < 10 || age > 120 || w < 20 || w > 500 || h < 50 || h > 250) {
      warn("Values out of range.")
      break(())
    }

    val male = Option(sexChoice).flatMap(c => Option(c.getValue)).contains("Male")
    // Mifflin–St Jeor
    val bmr = if (male) 10*w + 6.25*h - 5*age + 5
              else       10*w + 6.25*h - 5*age - 161

    val activityFactor = Option(activityChoice).map(_.getSelectionModel.getSelectedIndex).getOrElse(0) match {
      case 0 => 1.20; case 1 => 1.375; case 2 => 1.55; case 3 => 1.725; case 4 => 1.90; case _ => 1.20
    }
    val tdee = bmr * activityFactor

    val goalAdj = Option(goalChoice).map(_.getSelectionModel.getSelectedIndex).getOrElse(1) match {
      case 0 => -0.20   // cut
      case 1 =>  0.00   // maintain
      case 2 =>  0.15   // bulk
      case _ =>  0.00
    }
    val target = tdee * (1 + goalAdj)

    setText(bmrLabel,    f"BMR: ${bmr}%.0f kcal/day")
    setText(tdeeLabel,   f"TDEE: ${tdee}%.0f kcal/day")
    setText(targetLabel, f"Target Calories: ${target}%.0f kcal/day")
  }

  @FXML def handleReset(): Unit = {
    clear(ageField); clear(weightField); clear(heightField)
    if (sexChoice != null)      sexChoice.getSelectionModel.selectFirst()
    if (activityChoice != null) activityChoice.getSelectionModel.select(0)
    if (goalChoice != null)     goalChoice.getSelectionModel.select(1)
    setText(bmrLabel, "BMR: —"); setText(tdeeLabel, "TDEE: —"); setText(targetLabel, "Target Calories: —")
  }

  // helpers
  private def parseInt(tf: TextField): Option[Int] =
    Try(Option(tf).flatMap(t => Option(t.getText)).map(_.trim).getOrElse("").toInt).toOption
  private def parseDouble(tf: TextField): Option[Double] =
    Try(Option(tf).flatMap(t => Option(t.getText)).map(_.trim.replace(',','.')).getOrElse("").toDouble).toOption
  private def clear(tf: TextField): Unit = if (tf != null) tf.clear()
  private def setText(lb: Label, s: String): Unit = if (lb != null) lb.setText(s)
  private def setText(tf: TextField, s: String): Unit = if (tf != null) tf.setText(s)
  private def format1(d: Double): String = f"$d%.1f"
  private def warn(msg: String): Unit = {
    val a = new Alert(Alert.AlertType.WARNING)
    a.setTitle("Calculator"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait()
  }
}
