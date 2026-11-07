package controller

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control._
import javafx.util.converter.DoubleStringConverter
import javafx.beans.property.{SimpleDoubleProperty, SimpleStringProperty}
import javafx.collections.{FXCollections, ObservableList}
import java.net.URL
import java.util.ResourceBundle
import scala.util.Try
import models.{Food, Validators}
import repository.FoodRepository

/** Controller for MainLayout.fxml (Foods tab) */
class MainController extends Initializable {

  // --- FXML-wired controls (fx:id must match your FXML) ---
  @FXML private var table:   TableView[Food]                 = null
  @FXML private var colName: TableColumn[Food, String]       = null
  @FXML private var colCal:  TableColumn[Food, java.lang.Double] = null
  @FXML private var colP:    TableColumn[Food, java.lang.Double] = null
  @FXML private var colC:    TableColumn[Food, java.lang.Double] = null
  @FXML private var colF:    TableColumn[Food, java.lang.Double] = null

  @FXML private var nameField: TextField = null
  @FXML private var calField:  TextField = null
  @FXML private var pField:    TextField = null
  @FXML private var cField:    TextField = null
  @FXML private var fField:    TextField = null
  @FXML private var selLabel:  Label     = null

  // --- Model/repo + table backing list ---
  private var repo: FoodRepository = _
  private val data: ObservableList[Food] = FXCollections.observableArrayList[Food]()

  // current search query (lowercased)
  private var currentQuery: String = ""

  // ---------- lifecycle ----------
  override def initialize(url: URL, rb: ResourceBundle): Unit = {
    // columns (done in code; do NOT set cellValueFactory in FXML)
    if (colName != null) colName.setCellValueFactory(cd => new SimpleStringProperty(cd.getValue.name))
    if (colCal  != null) colCal .setCellValueFactory(cd => new SimpleDoubleProperty(cd.getValue.calories).asObject())
    if (colP    != null) colP   .setCellValueFactory(cd => new SimpleDoubleProperty(cd.getValue.protein).asObject())
    if (colC    != null) colC   .setCellValueFactory(cd => new SimpleDoubleProperty(cd.getValue.carbs).asObject())
    if (colF    != null) colF   .setCellValueFactory(cd => new SimpleDoubleProperty(cd.getValue.fat).asObject())

    if (table != null) table.setItems(data)

    // Selected-row label
    if (selLabel != null && table != null) {
      table.getSelectionModel.selectedItemProperty.addListener { (_, _, v) =>
        val txt =
          if (v == null) "Selected: (none)"
          else f"Selected: ${v.name} — ${v.calories}%.0f kcal | P:${v.protein} C:${v.carbs} F:${v.fat}"
        selLabel.setText(txt)
      }
    }

    // (Optional) live search as user types in nameField
    if (nameField != null) {
      nameField.textProperty.addListener { (_, _, newVal) =>
        currentQuery = Option(newVal).getOrElse("").trim.toLowerCase
        applyFilter()
      }
    }
  }

  /** MainApp must call this immediately after loading FXML */
  def initWith(fr: FoodRepository): Unit = {
    this.repo = fr
    currentQuery = ""
    applyFilter()
  }

  // ---------- filtering & refresh ----------
  /** Recomputes table items based on currentQuery */
  private def applyFilter(): Unit = {
    val all = if (repo != null) repo.all() else Seq.empty
    val filtered =
      if (currentQuery.isEmpty) all
      else all.filter(f => f.name.toLowerCase.contains(currentQuery))
    data.setAll(filtered: _*)
  }

  /** Convenience alias that keeps current filter */
  private def refresh(): Unit = applyFilter()

  // ---------- actions ----------
  /** Triggered by the Search button or Enter on nameField */
  @FXML def handleSearch(): Unit = {
    currentQuery = text(nameField).toLowerCase
    applyFilter()
    if (!data.isEmpty) table.getSelectionModel.select(0)
  }

  @FXML def handleAdd(): Unit = {
    val name = text(nameField)
    val cal  = text(calField)
    val p    = text(pField)
    val c    = text(cField)
    val f    = text(fField)
    Validators.validateFoodInputs(name, cal, p, c, f) match {
      case Right(food) =>
        repo.add(food)
        refresh() // respects currentQuery
        // clear inputs
        List(nameField, calField, pField, cField, fField).foreach(tf => if (tf != null) tf.clear())
      case Left(msg) =>
        warn("Invalid input", msg)
    }
  }

  @FXML def handleDelete(): Unit = {
    if (table == null) return
    val sel = table.getSelectionModel.getSelectedItem
    if (sel != null) {
      repo.deleteByName(sel.name)
      refresh()
    } else {
      info("No selection", "Select a row to delete.")
    }
  }

  // ---------- helpers ----------
  private def text(tf: TextField): String =
    Option(tf).flatMap(t => Option(t.getText)).map(_.trim).getOrElse("")

  private def info(h: String, m: String): Unit =
    new Alert(Alert.AlertType.INFORMATION) {{ setTitle("Info"); setHeaderText(h); setContentText(m) }}.showAndWait()

  private def warn(h: String, m: String): Unit =
    new Alert(Alert.AlertType.WARNING) {{ setTitle("Warning"); setHeaderText(h); setContentText(m) }}.showAndWait()
}
