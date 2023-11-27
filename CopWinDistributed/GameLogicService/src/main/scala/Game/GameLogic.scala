package Game

import org.slf4j.LoggerFactory

/**
 * Contains the logic for managing the game's state and processing player moves.
 */
object GameLogic {

  // Setup logging
  private val logger = LoggerFactory.getLogger(this.getClass)

  private type Graph = Map[Int, (Boolean, List[Int])]

  /**
   * Represents the state of the game at any given moment.
   * Contains the original and perturbed graphs, player positions, and game status.
   *
   * @param originalGraph The original graph of the game.
   * @param perturbedGraph A perturbed version of the original graph.
   * @param policemanPosition Current position of the policeman.
   * @param thiefPosition Current position of the thief.
   * @param gameEnded Flag indicating if the game has ended.
   * @param winner The winner of the game, if applicable.
   */
  case class GameState(
                        originalGraph: Graph,
                        perturbedGraph: Graph,
                        policemanPosition: Int,
                        thiefPosition: Int,
                        gameEnded: Boolean,
                        winner: Option[String] = None
                      )

  /**
   * Initializes the game with starting positions for Policeman and Thief.
   *
   * @param originalGraph The original graph of the game.
   * @param perturbedGraph The perturbed graph of the game.
   * @param policemanStart Starting position of the policeman.
   * @param thiefStart Starting position of the thief.
   * @return The initial game state.
   */
  def initializeGame(
                      originalGraph: Graph,
                      perturbedGraph: Graph,
                      policemanStart: Int,
                      thiefStart: Int
                    ): GameState = {
    logger.info("Initializing game with Policeman at position {} and Thief at position {}", policemanStart, thiefStart)
    GameState(
      originalGraph,
      perturbedGraph,
      policemanStart,
      thiefStart,
      gameEnded = false
    )
  }

  /**
   * Processes a move made by a player (Policeman or Thief).
   *
   * @param currentState The current state of the game.
   * @param player The player making the move ("Policeman" or "Thief").
   * @param moveTo The target position for the move.
   * @return The updated game state after processing the move.
   */
  def processMove(currentState: GameState, player: String, moveTo: Int): GameState = {
    logger.debug("Processing move for {} to position {}", player, moveTo)
    // Check if the moveTo node is a valid and reachable node in the graph
    val isMoveValid = player match {
      case "Policeman" =>
        currentState.originalGraph.get(currentState.policemanPosition).exists(_._2.contains(moveTo))
      case "Thief" =>
        currentState.originalGraph.get(currentState.thiefPosition).exists(_._2.contains(moveTo))
      case _ => false
    }

    if (!isMoveValid) {
      // Invalid move, game ends - set the winner to the other player
      val winner = if (player == "Policeman") Some("Thief") else Some("Policeman")
      logger.warn("Invalid move by {}. Game ended. Winner: {}", player, winner.get)
      return currentState.copy(gameEnded = true, winner = winner)
    }

    // Update the position of the player based on a valid move
    val updatedState = player match {
      case "Policeman" => currentState.copy(policemanPosition = moveTo)
      case "Thief"     => currentState.copy(thiefPosition = moveTo)
      case _           => currentState
    }

    // Check if this move ends the game
    val (gameEnded, winner) = checkGameStatus(updatedState)
    logger.debug("Game status after move: gameEnded={}, winner={}", gameEnded, winner.getOrElse("None"))
    updatedState.copy(gameEnded = gameEnded, winner = winner)
  }

  /**
   * Checks the current state of the game to determine if it has ended and who the winner is.
   *
   * @param currentState The current state of the game.
   * @return A tuple of Boolean (indicating if the game has ended) and an Option[String] for the winner.
   */
  def checkGameStatus(currentState: GameState): (Boolean, Option[String]) = {
    // If the game has already ended, return the current status
    if (currentState.gameEnded) {
      logger.debug("Game already ended. Winner: {}", currentState.winner.getOrElse("None"))
      return (true, currentState.winner)
    }

    val thiefValuableData = currentState.originalGraph(currentState.thiefPosition)._1
    val thiefNeighbors = currentState.originalGraph(currentState.thiefPosition)._2
    val copNeighbors = currentState.originalGraph(currentState.policemanPosition)._2

    if (currentState.thiefPosition == currentState.policemanPosition) {
      logger.info("Policeman caught the Thief. Policeman wins.")
      (true, Some("Policeman")) // Policeman wins by catching the Thief
    } else if (thiefValuableData) {
      logger.info("Thief reached valuable data. Thief wins.")
      (true, Some("Thief")) // Thief wins by reaching valuable data
    } else if (thiefNeighbors.isEmpty || copNeighbors.isEmpty) {
      val winner = if (thiefNeighbors.isEmpty) "Policeman" else "Thief"
      logger.info("Player {} has no moves. {} wins.", if (winner == "Policeman") "Thief" else "Policeman", winner)
      (true, Some(winner)) // One of the players has no moves
    } else {
      logger.debug("Game continues. No winner yet.")
      (false, None) // Game continues
    }
  }
}