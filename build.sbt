name := """play-scala-starter-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.11.12", "2.12.7")

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2",
  "com.h2database" % "h2" % "1.4.196" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.0",
  "com.typesafe.slick" %% "slick" % "3.2.0",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0",
  "mysql" % "mysql-connector-java" % "8.0.13",
  "org.scalacheck" %% "scalacheck" % "1.14.0",
  "net.sourceforge.cobertura" % "cobertura" % "2.1.1" % Test

)
