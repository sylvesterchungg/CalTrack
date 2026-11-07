package controller

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{TextField, Alert,ChoiceBox}
import java.net.URL
import java.util.ResourceBundle
import services.{UserRepository, CsvUserRepository}
import models.User
import scala.util.Try

class SignUpController extends Initializable {
  @FXML private var umField:        TextField = null  // username
  @FXML private var pwField:        TextField = null //password
  @FXML private var confirmpwField: TextField = null // confirm password
  @FXML private var ageField:       TextField = null // age
  @FXML private var weightField:    TextField = null // weight
  @FXML private var heightTextField: TextField = null // height
  @FXML private var genderChoice:   ChoiceBox[String] = null //gender

  var onClose:    () => Unit = () => ()
  var onSignedUp: () => Unit = () => ()
  
  // repository for user management
  private var repo: UserRepository = null
  def initWith(r: UserRepository): Unit = this.repo = r


  override def initialize(url: URL, rb: ResourceBundle): Unit = {
      if (genderChoice != null) {
      genderChoice.getItems.setAll("Male", "Female", "Other")
      genderChoice.getSelectionModel.select("Other")
      if (confirmpwField != null) confirmpwField.setOnAction(_ => handleSignUp())
      if (heightTextField != null) heightTextField.setOnAction(_ => handleSignUp())
    }
   }

  @FXML def handleSignUp(): Unit = {
    println(s"[SignUp] attempt user='${getText(umField)}'")
    val username = getText(umField)
    val p1       = getText(pwField)
    val p2       = getText(confirmpwField)
    val ageOpt   = parseInt(ageField)
    val wOpt     = parseDouble(weightField)
    val hOpt     = parseDouble(heightTextField)
    val gender   = if (genderChoice != null) Option(genderChoice.getValue).getOrElse("Other") else "Other"

        // Basic validation
    val err =
    if (username.isEmpty) Some("Username is required.")
    else if (p1.isEmpty) Some("Password is required.")
    else if (p1 != p2) Some("Passwords do not match.")
    else if (ageOpt.isEmpty || ageOpt.exists(a => a < 10 || a > 120)) Some("Please enter a valid age (10–120).")
    else if (wOpt.isEmpty  || wOpt.exists(w => w < 20 || w > 500))   Some("Please enter a valid weight in kg (20–500).")
    else if (hOpt.isEmpty  || hOpt.exists(h => h < 50 || h > 250))   Some("Please enter a valid height in cm (50–250).")
    else None

    if (err.isDefined) { warn(err.get); return }

    val age    = ageOpt.get
    val weight = wOpt.get
    val height = hOpt.get

    // Hash password if repo supports it
    val hash = repo match {
      case c: CsvUserRepository => c.hashPassword(p1)
      case _                    => p1 // fallback; prefer hashing
    }

    val u = User(username = username, passwordHash = hash,age = age, gender = gender, weight = weight, height = height)
        repo.save(u) match {
        case Right(_) =>
          info("Account created", s"Welcome, $username!")
          onSignedUp()
        case Left(msg) =>
          warn(msg)
      }
    }
  @FXML def handleClose(): Unit = if (onClose != null) onClose()

  private def getText(tf: TextField): String =
    Option(tf).flatMap(t => Option(t.getText)).map(_.trim).getOrElse("")

  private def parseInt(tf: TextField): Option[Int] =
    Try(getText(tf).toInt).toOption

  private def parseDouble(tf: TextField): Option[Double] = {
    // accept "70,5" as "70.5" as well
    val s = getText(tf).replace(',', '.')
    Try(s.toDouble).toOption
  }

  private def warn(msg: String): Unit = {
    val a = new Alert(Alert.AlertType.WARNING)
    a.setTitle("Sign Up"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait()
  }

  private def info(h: String, m: String): Unit = {
    val a = new Alert(Alert.AlertType.INFORMATION)
    a.setTitle("Sign Up"); a.setHeaderText(h); a.setContentText(m); a.showAndWait()
  }

}