ThisBuild / scalaVersion := "3.4.2"

lazy val root = (project in file("."))
  .settings(
    name := "OptimumCalorie",
    Compile / mainClass := Some("app.MainApp"),
    // Ensure Java 21 bytecode
    ThisBuild / javacOptions ++= Seq("--release", "21"),
    // Fork a new JVM for JavaFX
    Compile / run / fork := true,
    libraryDependencies ++= {
      val os = System.getProperty("os.name").toLowerCase
      val platform = if (os.contains("win")) "win" else if (os.contains("mac")) "mac" else "linux"
      Seq(
        "org.scalafx" %% "scalafx" % "21.0.0-R32",
        "org.openjfx" % "javafx-controls" % "21.0.8" classifier "win",
        "org.openjfx" % "javafx-fxml"      % "21.0.8" classifier "win",
        "org.openjfx" % "javafx-graphics"  % "21.0.8" classifier "win"
      )
    }
  )
