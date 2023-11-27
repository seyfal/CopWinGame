name := "GameLogicService"

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

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-s3" % "1.12.470" // Replace "1.12.x" with the latest version number
)

// Define Scala Compiler options.
scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs
  "-feature", // Emit warning and location for usages of features that should be imported explicitly
)

// Define JVM options for running your project.
//compileOrder := CompileOrder.JavaThenScala
//test / fork := true
//run / fork := true
run / javaOptions ++= Seq(
  "-Xms512M", // Initial JVM heap size
  "-Xmx2G", // Maximum JVM heap size
  "-XX:+UseG1GC" // Use G1 Garbage Collector
)

// Define the main class. Replace with the actual main class of your application.
Compile / mainClass := Some("Main")

val jarName = "RandomWalk.jar"
assembly/assemblyJarName := jarName

//Merging strategies
ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}
