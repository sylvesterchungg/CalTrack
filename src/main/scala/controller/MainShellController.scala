package controller

import javafx.fxml.{FXML, FXMLLoader, Initializable}
import javafx.scene.Parent
import javafx.scene.control.{Button, Tab, TabPane, Label}
import java.net.URL
import java.util.ResourceBundle
import repository.{FoodRepository, RecipeRepository}
import services.{DailyDiary, GoalService}

class MainShellController extends Initializable {
  @FXML private var tabPane: TabPane = null
  @FXML private var dashboardTab: Tab = null
  @FXML private var foodsTab: Tab = null
  @FXML private var recipesTab: Tab = null
  @FXML private var logoutBtn: Button = null
  @FXML private var calcTab: Tab = null

  private var dashCtl: Option[DashboardController] = None
  
  var onLogout: () => Unit = () => ()

  override def initialize(url: URL, rb: ResourceBundle): Unit = ()

  /** Called by MainApp after load */
  def initWith(foodRepo: FoodRepository, recipeRepo: RecipeRepository,diary: DailyDiary,goals: GoalService): Unit = {
    loadTab(foodsTab,   "MainLayout.fxml")   { c => c.asInstanceOf[MainController].initWith(foodRepo) }
    loadTab(recipesTab, "RecipeTab.fxml") { c => c.asInstanceOf[RecipeController].initWith(foodRepo, recipeRepo,diary) }
    loadTab(calcTab,    "Calculator.fxml"){ _ => () }
    loadTab(dashboardTab, "Dashboard.fxml") { ctl =>  ctl match {case d: DashboardController => d.initWith(diary, goals)
        case _ => ()
      }
    }
  }
  

  private def loadTab(tab: Tab, path: String)(configure: Any => Unit): Unit = {
    if (tab == null) return
    val cl  = Thread.currentThread.getContextClassLoader
    val url = Option(cl.getResource(path)).orElse(Option(cl.getResource(s"fxml/$path")))
      .getOrElse(throw new IllegalStateException(s"Resource not found: $path or fxml/$path"))
    val l = new FXMLLoader(url)
    val n: Parent = l.load[Parent]()
    configure(l.getController[Any]())
    tab.setContent(n)
  }

  

  @FXML def handleLogout(): Unit = onLogout()
}
