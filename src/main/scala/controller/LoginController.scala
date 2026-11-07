package controller

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Alert, Button, Label, PasswordField, TextField}
import java.net.URL
import java.util.ResourceBundle
import scalafx.Includes.jfxTextField2sfx
import services.{Session, CsvUserRepository, UserRepository}
import models.User
import repository.UserCsvRepository


class LoginController extends Initializable {
  @FXML private var usernameField: TextField = null
  @FXML private var passwordField: PasswordField = null
  @FXML private var errorLabel: Label = null
  @FXML private var loginBtn: Button = null
  @FXML private var signupBtn: Button = null

  // callbacks set by MainApp
  var onStart:  () => Unit = () => ()
  var onSignup: () => Unit = () => ()

  // hardcoded credentials
  private val correctUsername = "admin"
  private val correctPassword = "admin123"
  // repository for user management
  private var repo: UserRepository = null
  def initWith(r: UserRepository): Unit = this.repo = r

  override def initialize(url: URL, rb: ResourceBundle): Unit = {
    if (loginBtn != null) loginBtn.setDefaultButton(true)
  }

  @FXML def handleStart(): Unit = {
  val u = Option(usernameField).map(_.getText.trim).getOrElse("")
  val p = Option(passwordField).map(_.getText).getOrElse("")
  println(s"[Login] validating user='$u'")

  val ok = repo != null && repo.validate(u, p)
  println(s"[Login] validate -> $ok")

  if (ok) {
    // --- fetch the full user profile and store in session ---
    val userOpt: Option[User] = try {
      // If your concrete repo is CSV-based, use it directly
      repo match {
        case c: CsvUserRepository => c.find(u)                    // preferred
        case _                    => UserCsvRepository.find(u)    // fallback object, if present
      }
    } catch { case _: Throwable => None }

    userOpt.foreach(Session.setCurrentUser)

    // --- success alert + navigate ---
    val a = new Alert(Alert.AlertType.INFORMATION)
    a.setTitle("Login Successful")
    a.setHeaderText("Welcome!")
    a.setContentText("You have successfully logged in.")
    Option(loginBtn).flatMap(b => Option(b.getScene)).flatMap(s => Option(s.getWindow)).foreach(a.initOwner)
    a.showAndWait()

    onStart() // proceed to main shell
  } else {
    val a = new Alert(Alert.AlertType.ERROR)
    a.setTitle("Login Failed")
    a.setHeaderText("Invalid Credentials")
    a.setContentText("The username or password you entered is incorrect.")
    Option(loginBtn).flatMap(b => Option(b.getScene)).flatMap(s => Option(s.getWindow)).foreach(a.initOwner)
    a.showAndWait()

    if (usernameField != null) usernameField.clear()
    if (passwordField != null) passwordField.clear()
    if (errorLabel != null) { errorLabel.setText("Try again."); errorLabel.setVisible(true); errorLabel.setManaged(true) }
  }
}


  @FXML def handleSignup(): Unit = {
    Session.clear() // clear any previous session
    onSignup()
}
}