name := "PlayerManagmentService"

version := "0.1"

scalaVersion := "2.13.6"

// Define versions as variables for easy maintenance and updates
val scalaTestVersion = "3.2.15"
val typeSafeConfigVersion = "1.4.2"
val logbackVersion = "1.2.7"
val slf4jVersion = "2.0.7"
val json4sVersion = "4.0.6"
val akkaHttpVersion = "10.5.0"
val akkaVersion = "2.8.0"
val akkaHttpJson4sVersion = "1.39.2"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.json4s" %% "json4s-native" % json4sVersion,
  "org.json4s" %% "json4s-jackson" % json4sVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
)
