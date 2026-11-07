package controller

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control._
import javafx.collections.FXCollections
import java.net.URL
import java.util.ResourceBundle
import scala.util.Try
import models.{Food, Recipe}
import repository.{FoodRepository, RecipeRepository}
import services.DailyDiary
import scala.math.Ordering.Implicits.infixOrderingOps


class RecipeController extends Initializable {
  @FXML private var foodsList:     ListView[String] = null
  @FXML private var chosenList:    ListView[String] = null
  @FXML private var qtyField:      TextField = null
  @FXML private var recipeName:    TextField = null
  @FXML private var totalsArea:    TextArea = null
  @FXML private var searchField:   TextField = null
  @FXML private var addBtn:        Button = null
  @FXML private var removeBtn:     Button = null
  @FXML private var saverecipeBtn: Button = null
  @FXML private var mealChoice:    ChoiceBox[String] = null

  private var foodRepo: FoodRepository = null
  private var recipeRepo: RecipeRepository = null
  private var diary: services.DailyDiary = null 

  var onRecipeSaved: () => Unit = () => ()

  private val allNames    = FXCollections.observableArrayList[String]()
  private val chosenDisp  = FXCollections.observableArrayList[String]()
  private var chosenPairs = List.empty[(Food, Double)]

  override def initialize(url: URL, rb: ResourceBundle): Unit = {
    if (foodsList != null) foodsList.setItems(allNames)
    if (chosenList != null) chosenList.setItems(chosenDisp)
    if (qtyField != null)   qtyField.setText("100")   // default to 100 g
    if (totalsArea != null) totalsArea.setEditable(false)
    if (mealChoice != null) {
      mealChoice.getItems.setAll("Breakfast", "Lunch", "Dinner", "Snack")
      mealChoice.getSelectionModel.select("Lunch")
  }
  }

  /** Called by MainApp after load. */
   def initWith(fr: FoodRepository, rr: RecipeRepository, d: DailyDiary): Unit = {
    this.foodRepo   = fr
    this.recipeRepo = rr
    this.diary      = d
    refreshMasterList()
    updateTotals()
  }

//----------------UI Actions----------------
  @FXML def handleSearch(): Unit = {
    val q = text(searchField).toLowerCase
    if (q.isEmpty) {
      allNames.setAll(foodRepo.all().map(_.name): _*)
    } else {
      val filtered = foodRepo.all().map(_.name).filter(_.toLowerCase.contains(q))
      allNames.setAll(filtered: _*)
    }
  }

    @FXML def handleAdd(): Unit = {
      val selName = Option(foodsList).map(_.getSelectionModel.getSelectedItem).orNull
      if (selName == null || selName.trim.isEmpty) { warn("Pick a food first."); return }

      val grams = readGramsOrWarn()
      if (grams <= 0.0) { warn("Grams must be greater than 0."); return }

      foodRepo.all().find(_.name == selName).foreach { f =>
        chosenPairs = chosenPairs :+ (f -> grams)
        val kcal = f.calories * (grams / 100.0)
        chosenDisp.add(f"$selName — $grams%.0f g ($kcal%.0f kcal)")
        updateTotals()
        if (qtyField != null) qtyField.clear()
      }
    }
    @FXML def handleUpdateQty(): Unit = {
    val idx = Option(chosenList).map(_.getSelectionModel.getSelectedIndex).getOrElse(-1)
    if (idx < 0) { warn("Select an item to update."); return }

    val grams = readGramsOrWarn()          // default to 100 g if empty
    if (grams <= 0.0) { warn("Enter grams > 0"); return }

    val (f, _) = chosenPairs(idx)
    chosenPairs = chosenPairs.updated(idx, f -> grams)
    val kcal = f.calories * (grams / 100.0)
    chosenDisp.set(idx, f"${f.name} — $grams%.0f g ($kcal%.0f kcal)")
    updateTotals()
}

    @FXML def handleRemove(): Unit = {
    val idx = Option(chosenList).map(_.getSelectionModel.getSelectedIndex).getOrElse(-1)
    if (idx >= 0 && idx < chosenDisp.size()) {
      chosenDisp.remove(idx)
      chosenPairs = chosenPairs.patch(idx, Nil, 1)
      updateTotals()
    }
  }

  @FXML def handleSaveRecipe(): Unit = {
    val name = text(recipeName)
    if (name.isEmpty)        { warn("Please enter a recipe name."); return }
    if (chosenPairs.isEmpty) { warn("Add at least one ingredient."); return }

    val (kc, p, c, f) = totals()
    val ingredients   = chosenPairs.map(_._1)
    val recipe        = Recipe(name, kc, p, c, f, ingredients)
    recipeRepo.add(recipe)
    if (diary != null) diary.add(name, kc, p, c, f)
    onRecipeSaved()
    info("Recipe saved",
      s"$name\nkcal: ${kc.formatted("%.0f")}  P:${p.formatted("%.1f")}  C:${c.formatted("%.1f")}  F:${f.formatted("%.1f")}")
    chosenPairs = Nil
    chosenDisp.clear()
    if (recipeName != null) recipeName.clear()
    updateTotals()
  }

  // -------- helpers --------
  private def readGramsOrWarn(default: Double = 100.0): Double = {
  val raw = Option(qtyField).flatMap(tf => Option(tf.getText)).map(_.trim).getOrElse("")
  // keep only digits, dot/comma, minus (then normalize comma to dot)
  val normalized = raw.replaceAll("[^0-9.,-]", "").replace(',', '.')
  val grams =
    if (normalized.isEmpty) default
    else scala.util.Try(normalized.toDouble).getOrElse(Double.NaN)

  if (grams.isNaN || grams <= 0.0) {
    warn(s"Enter grams > 0 (got: '${raw}')")
    -1.0
  } else grams
}

  private def refreshMasterList(): Unit =
    allNames.setAll(foodRepo.all().map(_.name): _*)

  private def totals(): (Double, Double, Double, Double) =
  chosenPairs.foldLeft(0.0, 0.0, 0.0, 0.0) {
    case ((kc, p, c, f), (food, grams)) =>
    val k = grams / 100.0
    (kc + food.calories * k, p + food.protein * k, c + food.carbs * k, f + food.fat * k)
  }
  
  private def updateTotals(): Unit = {
    val (kc, p, c, f) = totals()
    if (totalsArea != null)
      totalsArea.setText(f"Items: ${chosenDisp.size}%d\nTotals — kcal:${kc}%.0f  P:${p}%.1f  C:${c}%.1f  F:${f}%.1f")
  }



  private def readGrams(): Double =
  scala.util.Try(text(qtyField).replace(',', '.').toDouble).getOrElse(-1.0)

  private def text(tf: TextField): String =
    Option(tf).flatMap(t => Option(t.getText)).map(_.trim).getOrElse("")

  private def warn(msg: String): Unit = {
    val a = new Alert(Alert.AlertType.WARNING)
    a.setTitle("Recipes"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait()
  }
  private def info(h: String, m: String): Unit = {
    val a = new Alert(Alert.AlertType.INFORMATION)
    a.setTitle("Recipes"); a.setHeaderText(h); a.setContentText(m); a.showAndWait()
  }
}