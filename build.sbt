import Dependencies.allSpecified

// assembly plugin
lazy val assemblySettings = Seq(
  // jar name
  assemblyJarName in assembly := ((name) map { (n) => n + ".jar" }).value,

  // no test during assembly
  test in assembly := {},

  // merge strategies
  assemblyMergeStrategy in assembly := {
    case "application.conf"                           => MergeStrategy.concat
    case "logback.xml"                                => MergeStrategy.first
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "net.qns.delphinus",
      scalaVersion := "2.11.12",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "twitter-spark-kafka",
    libraryDependencies ++= allSpecified
  )
  .settings(assemblySettings)
