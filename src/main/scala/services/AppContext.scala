package services
import java.nio.file.{Files, Paths}  
import repository._
import models._

final class AppContext(
  val foodRepo:   FoodRepository,
  val recipeRepo: RecipeRepository,
  val diary:      DailyDiary,
  val goals:      GoalService            
) {

  preload()

  private def preload(): Unit = {
    val foods = SeedData.loadFoodsFromResource("/db/food_data.txt")
    foods.foreach(foodRepo.add)
    println(s"[AppContext] Seeded foods: ${foods.size}")
  }

  // User CSV helpers (unchanged)
  def saveUser(u: User): Either[String, Unit] =
    UserCsvRepository.save(u)

  def validateLogin(username: String, password: String): Boolean =
    UserCsvRepository.validate(username, password)
}

object AppContext {
  def default(): AppContext = {
    // choose where to store foods.csv
    val dataDir  = Paths.get(System.getProperty("user.dir"), "data") // or Paths.get("data")
    Files.createDirectories(dataDir)                                       // ensure folder exists
    val foodsCsv = dataDir.resolve("foods.csv")

    val ctx = new AppContext(
      new CsvFoodRepository(foodsCsv),   
      new InMemoryRecipeRepository(),
      new DailyDiary(),
      new GoalService()
    )

    // seed only if empty (avoid duplicates)
    if (ctx.foodRepo.all().isEmpty) {
      val foods = SeedData.loadFoodsFromResource("/db/food_data.txt")
      foods.foreach(ctx.foodRepo.add)
      println(s"[AppContext] Seeded foods: ${foods.size}")
    } else {
      println(s"[AppContext] Foods already present: ${ctx.foodRepo.all().size}, skipping seed.")
    }

    ctx
  }
}