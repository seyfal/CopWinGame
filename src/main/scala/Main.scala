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

import ConfigurationLoader.{getOriginalEdgesPath, getOriginalNodesPath, getPerturbedEdgesPath, getPerturbedNodesPath}
import GameLogic.{checkGameStatus, initializeGame}
import LoadMethods.createGraph
import org.slf4j.LoggerFactory

// Define the main object that will be the entry point of the application.
object Main {

  // Initialize the logger for the Main object.
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val methodName = "Main"

  // Define the main method, which is the entry point.
  def main(args: Array[String]): Unit = {
    // Begin a try-catch block to catch and handle any exceptions that occur within the main method.
    try {
      // Log the beginning of the execution.
      logger.info(s"$methodName: Starting the program")

      // load the graph from the given files
      val originalGraph = createGraph(getOriginalNodesPath, getOriginalEdgesPath)
      val perturbedGraph = createGraph(getPerturbedNodesPath, getPerturbedEdgesPath)

      var gameState = initializeGame(originalGraph, perturbedGraph)

      // Game loop
      while (!gameState.gameEnded) {
        // Logic for players to make moves
        // Example: gameState = GameLogic.processMove(gameState, "Thief", nextThiefPosition)

        // Check game status
        val (ended, winner) = checkGameStatus(gameState)
        gameState = gameState.copy(gameEnded = ended)

        // Logic to handle the end of the game, announce winner, etc.
      }
      // Log that the program has finished successfully.
      logger.info(s"$methodName: Program finished successfully")
    } catch {
      // Catch any exceptions that might have been thrown during the execution of the main method.
      case e: Exception =>
        // Log the exception at the error level.
        logger.error(s"$methodName: An exception occurred:", e)
        // Rethrow the exception if further handling is needed or to terminate the program with an error status.
        throw e
    } finally {
      // The finally block is executed after the try-catch block regardless of whether an exception was thrown.
      // It's often used for cleanup activities.
      logger.debug(s"$methodName: Exiting the program")
    }
  }
}
