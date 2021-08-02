inThisBuild(
  List(
    name := "scaffeine",
    description := "Thin Scala wrapper for Caffeine.",
    organization := "com.github.blemale",
    homepage := Some(url("https://github.com/blemale/scaffeine")),
    licenses := List(
      "Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")
    ),
    startYear := Some(2016),
    developers := List(
      Developer(
        "blemale",
        "Bastien LEMALE",
        "blemale@protonmail.ch",
        url("https://github.com/blemale")
      )
    )
  )
)

scalaVersion := "2.11.12"
crossScalaVersions := Seq("2.11.12", "2.12.14", "2.13.6", "3.0.1")

libraryDependencies ++=
  Seq(
    "com.github.ben-manes.caffeine" % "caffeine"                % CaffeineVersion.value,
    "org.scala-lang.modules"       %% "scala-java8-compat"      % "1.0.0",
    "org.scala-lang.modules"       %% "scala-collection-compat" % "2.5.0",
    "com.google.code.findbugs"      % "jsr305"                  % "3.0.2" % Provided,
    "org.scalactic"                %% "scalactic"               % "3.2.9" % Test,
    "org.scalatest"                %% "scalatest"               % "3.2.9" % Test
  )

scalafmtOnCompile := true

scalacOptions := {
  val opts     = scalacOptions.value
  val fatalw   = "-Xfatal-warnings"
  val target2x = "-target:jvm-1.8"
  val target3x = "-release:8"

  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) => opts :+ target2x
    case Some((2, _))  => opts.filterNot(Set(fatalw)) :+ target2x
    case _             => opts.filterNot(Set(fatalw)) :+ target3x
  }
}

Compile / console / scalacOptions --= Seq(
  "-Ywarn-unused:imports",
  "-Xfatal-warnings"
)
