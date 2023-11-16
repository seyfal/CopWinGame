/*
    Copyright (c) 2023 Seyfal Sultanov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

/**
 * Provides utilities for loading and accessing configurations from the application's
 * configuration files. It determines the environment (AWS, EMR, local), checks for debug mode,
 * and configures the Spark session accordingly.
 */
object ConfigurationLoader {

  // Logger for the ConfigurationLoader
  private val logger = LoggerFactory.getLogger(this.getClass)

  // Initialize the configuration by loading it from the default location
  private val config = ConfigFactory.load()
  logger.info("ConfigurationLoader: Configuration set up successfully")

  // ################### ENVIRONMENT CHECKER ###################
  /**
   * Checks if the application is running on AWS by attempting to access the AWS metadata service.
   * @return Boolean representing whether the app is running on AWS or not.
   */
  private def checkIsOnAWS: Boolean = Try {
    val uri = new URI("http://169.254.169.254/latest/meta-data/")
    val connection = uri.toURL.openConnection()
    connection.setConnectTimeout(1000)
    connection.setReadTimeout(1000)

    val source = scala.io.Source.fromInputStream(connection.getInputStream)
    try source.mkString.nonEmpty
    finally source.close()
  }.getOrElse(false)

  // Call the check method and log the result
  private val isOnAWS = checkIsOnAWS
  logger.info(s"ConfigurationLoader: Is the application running on the AWS? $isOnAWS")

  /**
   * Checks if the application is running on AWS EMR by attempting to access the AWS user-data service.
   * @return Boolean representing whether the app is running on AWS EMR or not.
   */
  private def checkIsOnEMR: Boolean = Try {
    val uri = new URI("http://169.254.169.254/latest/user-data/")
    val connection = uri.toURL.openConnection()
    connection.setConnectTimeout(1000)
    connection.setReadTimeout(1000)

    val source = scala.io.Source.fromInputStream(connection.getInputStream)
    try {
      val userData = source.mkString
      userData.contains("emr-apps")
    } finally {
      source.close()
    }
  }.getOrElse(false)

  // Call the check method and log the result
  private val isOnEMR = checkIsOnEMR
  logger.info(s"ConfigurationLoader: Is the application running on the AWS EMR? $isOnEMR")

  // Determine the environment based on the checks
  private val environment = if (isOnAWS && isOnEMR) "cloud" else "local"
  logger.info(s"ConfigurationLoader: Environment set to $environment")

  // ################### DEBUG CHECKER ###################
  // Determine if the application is running in debug mode from the configuration
  private val debug = false //config.getBoolean("app.debug")
  logger.info(s"ConfigurationLoader: Is the application running in debug mode? $debug")

  // ################### FINAL PATHS SETTING ###################
  // Load appropriate configuration section based on debug mode and environment
  private val appConfig = (debug, environment) match {
    case (true, "local") => config.getConfig("app.local.debug")
    case (false, "local") => config.getConfig("app.local.release")
    case (_, "cloud") => config.getConfig("app.cloud")
    case _ => throw new IllegalArgumentException("Invalid configuration or environment settings")
  }
  logger.info("ConfigurationLoader: Configuration section loaded successfully")

  def isRunningOnCloud: Boolean = {
    environment == "cloud"
  }

  // ################### COMMON CONFIG ###################
  /**
   * Retrieves the number of random walks to perform from the configuration.
   * @return The number of random walks as an Int.
   */
  def getNumRandomWalks: Int = {
    val numRandomWalks = config.getInt("app.common.NumRandomWalks")
    logger.info(s"ConfigurationLoader: Number of random walks retrieved: $numRandomWalks")
    numRandomWalks
  }

  /**
   * Retrieves the similarity threshold for the random walks from the configuration.
   * @return The similarity threshold as a Double.
   */
  def getSimilarityThreshold: Double = {
    val similarityThreshold = config.getDouble("app.common.SimilarityThreshold")
    logger.info(s"ConfigurationLoader: Similarity threshold retrieved: $similarityThreshold")
    similarityThreshold
  }

  /**
   * Retrieves the number of steps for each random walk from the configuration.
   * @return The number of steps per random walk as an Int.
   */
  def getNumRandomWalkSteps: Int = {
    val numRandomWalkSteps = config.getInt("app.common.NumRandomWalkSteps")
    logger.info(s"ConfigurationLoader: Number of random walk steps retrieved: $numRandomWalkSteps")
    numRandomWalkSteps
  }

  /**
   * Retrieves the teleportation parameter C for the random walks from the configuration.
   * @return The teleportation parameter C as a Double.
   */
  def getC: Double = {
    val c = config.getDouble("app.common.C")
    logger.info(s"ConfigurationLoader: Teleportation parameter C retrieved: $c")
    c
  }

  /**
   * Retrieves the maximum depth for the random walks from the configuration.
   * @return The maximum depth as an Int.
   */
  def getMaxDepth: Int = {
    val maxDepth = config.getInt("app.common.MaxDepth")
    logger.info(s"ConfigurationLoader: Maximum depth retrieved: $maxDepth")
    maxDepth
  }

  /**
   * Retrieves the threshold for the random walks from the configuration.
   * @return The threshold as a Double.
   */
  def getThreshold: Double = {
    val threshold = config.getDouble("app.common.Threshold")
    logger.info(s"ConfigurationLoader: Threshold retrieved: $threshold")
    threshold
  }

  // ################### FILE PATH RETRIEVER ###################
  def getOriginalGraphFilePath: String = {
    // If we're in debug mode or if there's no specific debug/release distinction,
    // simply return the 'originalGraphFilePath' from the respective environment.
    if (debug || !appConfig.hasPath("release.originalGraphFilePath")) {
      appConfig.getString("originalGraphFilePath")
    } else {
      appConfig.getString("release.originalGraphFilePath")
    }
  }

  def getPerturbedGraphFilePath: String = {
    // Similar to the original graph file path, check for debug mode or lack of specific paths
    if (debug || !appConfig.hasPath("release.perturbedGraphFilePath")) {
      appConfig.getString("perturbedGraphFilePath")
    } else {
      appConfig.getString("release.perturbedGraphFilePath")
    }
  }

  def getOriginalNodesPath: String = {
    if (debug || !appConfig.hasPath("release.originalNodesPath")) {
      appConfig.getString("originalNodesPath")
    } else {
      appConfig.getString("release.originalNodesPath")
    }
  }

  def getOriginalEdgesPath: String = {
    if (debug || !appConfig.hasPath("release.originalEdgesPath")) {
      appConfig.getString("originalEdgesPath")
    } else {
      appConfig.getString("release.originalEdgesPath")
    }
  }

  def getPerturbedNodesPath: String = {
    if (debug || !appConfig.hasPath("release.perturbedNodesPath")) {
      appConfig.getString("perturbedNodesPath")
    } else {
      appConfig.getString("release.perturbedNodesPath")
    }
  }

  def getPerturbedEdgesPath: String = {
    if (debug || !appConfig.hasPath("release.perturbedEdgesPath")) {
      appConfig.getString("perturbedEdgesPath")
    } else {
      appConfig.getString("release.perturbedEdgesPath")
    }
  }

  /**
   * Retrieves the output directory path from the configuration.
   *
   * @return String representing the output directory path.
   */
  def getOutputDirectory: String = {
    val directoryKey = if (debug || !appConfig.hasPath("release.outputDirectory")) {
      "outputDirectory"
    } else {
      "release.outputDirectory"
    }

    val directoryPath = appConfig.getString(directoryKey)
    logger.info(s"Output directory path retrieved: $directoryPath")
    directoryPath
  }

  /**
   * Generates a file name based on the current time and date.
   * This is used for naming output files in a way that they are unique and identifiable.
   *
   * @return String representing the generated file name.
   */
  def generateFileName(): String = {
    val currentTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
    val fileName = s"MITMStatistics_${currentTime.format(formatter)}.txt"
    logger.info(s"Generated file name: $fileName")
    fileName
  }

}
