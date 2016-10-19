import sbt._

object Dependencies {
  val CaffeineVersion = "2.3.3"

  val Caffeine = "com.github.ben-manes.caffeine" % "caffeine" % CaffeineVersion
  val Jsr305 = "com.google.code.findbugs" % "jsr305" % "3.0.1"
  val Scalactic = "org.scalactic" %% "scalactic" % "2.2.6"
  val Scalatest = "org.scalatest" %% "scalatest" % "2.2.6"
}
