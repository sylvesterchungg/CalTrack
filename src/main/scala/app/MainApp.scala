package app

import scala.compiletime.uninitialized
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.image.Image
import scalafx.Includes._
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent}
import javafx.scene.control.{Menu, MenuBar, MenuItem}
import javafx.scene.layout.BorderPane
import services.AppContext
import controller._
import scalafx.Includes.jfxBorderPane2sfx
import services.{AppContext, CsvUserRepository, UserRepository}


object MainApp extends JFXApp3 {

  private val ctx = AppContext.default()
  private def clRes(path: String): java.net.URL = {
    val candidates = List(path, if (path.startsWith("fxml/")) path.drop(5) else s"fxml/$path")
    val cl = Thread.currentThread().getContextClassLoader
    val url = candidates.view.map(cl.getResource).find(_ != null).orNull
    println(s"[CalTrack] resource $path -> " + (if (url == null) "NOT FOUND" else url.toString))
    if (url == null) throw new IllegalStateException(s"Resource not found: $path (tried: ${candidates.mkString(", ")})")
    url
  }
  private def loadAsScene(fxml: String): (Parent, FXMLLoader) = {
  val l = new FXMLLoader(clRes(fxml))
  val root = l.load[Parent]()
  (root, l)
  }
  
  private var mainScene: Scene = uninitialized           
  private var rootPane: BorderPane = uninitialized
  private val userRepo: UserRepository = new CsvUserRepository()
  println("[MainApp] userRepo id = " + System.identityHashCode(userRepo))

  private def showCenter(path: String)(configure: Any => Unit = _ => ()): Unit = {
  val loader = new FXMLLoader(clRes(path))
  val node   = loader.load[Parent]()
  configure(loader.getController[Any]())
  rootPane.setCenter(node)

  // fit window to the new content
    scalafx.application.Platform.runLater(() => {
    stage.sizeToScene()
    // optional: lock minimum to current so it can’t shrink smaller than the layout
    stage.setMinWidth(stage.getWidth)
    stage.setMinHeight(stage.getHeight)
  })
}


  // Screens
  private def openLogin(): Unit = {
    stage.setTitle("CalTrack — Login")
    stage.resizable = false // prevent resizing during login
    showCenter("Login.fxml") { c =>
      val lc = c.asInstanceOf[LoginController]
      lc.initWith(userRepo)
      lc.onStart  = () => openMainShell()
      lc.onSignup = () => openSignUp()
    }
  }
  private def openSignUp(): Unit = {
    stage.setTitle("CalTrack — Sign Up")
    stage.resizable = false // prevent resizing during sign up
    showCenter("SignUp.fxml") { c =>
      val sc = c.asInstanceOf[SignUpController]
      sc.initWith(userRepo)
      sc.onClose    = () => openLogin()
      sc.onSignedUp = () => openLogin()
    }
  }

  private def openMainShell(): Unit = {
  stage.setTitle("CalTrack — Dashboard")
  showCenter("MainShell.fxml") { c =>
  val sh = c.asInstanceOf[MainShellController]
  sh.initWith(ctx.foodRepo, ctx.recipeRepo, ctx.diary, ctx.goals)
  sh.onLogout = () => openLogin()}
}
  // when opening Foods (MainLayout.fxml)
  private def openMainLayout(): Unit = {
    stage.setTitle("CalTrack — Foods (Main Layout)")
    showCenter("MainLayout.fxml") { c =>
      c.asInstanceOf[controller.MainController].initWith(ctx.foodRepo)
    }
  }
  // when opening FoodTab.fxml
  private def openFoodTab(): Unit = {
    stage.setTitle("CalTrack — Foods (FoodTab)")
    showCenter("FoodTab.fxml") { c =>
      c.asInstanceOf[controller.MainController].initWith(ctx.foodRepo)
    }
  }

  private def openRecipeTab(): Unit = {
    stage.setTitle("CalTrack — Recipes")
    showCenter("RecipeTab.fxml") { c =>
      c.asInstanceOf[controller.RecipeController].initWith(ctx.foodRepo, ctx.recipeRepo, ctx.diary)
    }
  }

  private def openAbout(): Unit = {
    stage.setTitle("CalTrack — About")
    showCenter("About.fxml") { c =>
    c.asInstanceOf[AboutController].onClose = () => openLogin()
    }
  }
  
  private def res(path: String): Option[String] =
    Option(getClass.getResource(path)).map(_.toExternalForm)
  

  override def start(): Unit = {
    rootPane = new BorderPane()
    mainScene = new Scene(rootPane)
    res("/styles/app.css").foreach(mainScene.getStylesheets.add)
    
    stage = new PrimaryStage {
      title = "CalTrack"
      scene = mainScene
    }
    val iconUrls = List(
      "/icons/appicon.png",
      "/icons/onlogo.png",
    ).flatMap(res)

    stage.getIcons.clear()
    iconUrls.foreach(u => stage.getIcons.add(new Image(u)))

    openLogin()
  }
}
