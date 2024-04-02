import sbt.Keys.scalacOptions

ThisBuild / scalaVersion := "2.13.12"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

scalacOptions += "-Wunused:imports"

val dependencies = new {

  val versions = new {
    val cassandraDriver  = "4.13.0"
    val akkaTyped = "2.9.0"
    val scalaTest = "3.2.15"
    val alpakkaCassandra= "7.0.1"
    val logback = "1.4.12"
    val faker = "0.5.3"
    val scalaFmt = "3.7.17"
    val akkaHttp = "10.6.0"
    val akkaStreamKafka = "5.0.0"
    val circeVersion = "0.14.1"
    val sendGrid = "4.10.1"
  }

  val alpakkaCassandra = "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % versions.alpakkaCassandra
  val akkaTyped = "com.typesafe.akka" %% "akka-actor-typed" % versions.akkaTyped
  val akkaTypedTest = "com.typesafe.akka" %% "akka-actor-testkit-typed" % versions.akkaTyped % Test
  val scalaTest = "org.scalatest" %% "scalatest" % versions.scalaTest % Test
  val logback = "ch.qos.logback" % "logback-classic" % versions.logback
  val faker = "com.github.pjfanning" %% "scala-faker" % versions.faker
  val scalafmt = "org.scalameta" % "sbt-scalafmt" % versions.scalaFmt
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % versions.akkaHttp
  val sprayHttp = "com.typesafe.akka" %% "akka-http-spray-json" % versions.akkaHttp
  val akkaStreamKafka = "com.typesafe.akka" %% "akka-stream-kafka" % versions.akkaStreamKafka
  val sendGrid = "com.sendgrid" % "sendgrid-java" % versions.sendGrid
  val circe = Seq (
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % versions.circeVersion)


}

  libraryDependencies ++= Seq(
    dependencies.alpakkaCassandra,
    dependencies.scalaTest,
    dependencies.akkaTyped,
    dependencies.akkaTypedTest,
    dependencies.logback,
    dependencies.faker,
    dependencies.akkaHttp,
    dependencies.sprayHttp,
    dependencies.akkaStreamKafka,
    dependencies.sendGrid
  ) ++ dependencies.circe

resolvers += "Akka library repository".at("https://repo.akka.io/maven")