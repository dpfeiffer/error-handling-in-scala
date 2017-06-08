lazy val root = project
  .in(file("."))
  .settings(
    name := "error-handling-in-scala",
    libraryDependencies ++= Seq(
      "com.chuusai"   %% "shapeless" % "2.3.2",
      "org.typelevel" %% "cats"      % "0.9.0"
    )
  )
