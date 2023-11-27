package APIs

import Game.GameLogic
import Game.GameLogic.GameState
import LoadPhase.FarthestNodes
import org.slf4j.LoggerFactory

/**
 * This class encapsulates the game logic service.
 * It manages the game state and processes game moves.
 */
class GameLogicService {

  // Logger for logging important events and errors
  private val logger = LoggerFactory.getLogger(getClass)

  // Alias for the graph type
  private type Graph = Map[Int, (Boolean, List[Int])]

  // The current state of the game, to be initialized when a new game starts
  private var gameState: GameState = _

  /**
   * Starts a new game with the given graphs and optional starting positions.
   *
   * @param originalGraph The original graph of the game.
   * @param perturbedGraph The perturbed graph of the game.
   * @param policemanStart Optional starting position of the policeman.
   * @param thiefStart Optional starting position of the thief.
   * @return A message indicating the start of the game and the starting positions.
   */
  def startNewGame(
                    originalGraph: Graph,
                    perturbedGraph: Graph,
                    policemanStart: Option[Int] = None,
                    thiefStart: Option[Int] = None
                  ): String = {
    // Determine start positions for Policeman and Thief
    val (pStart, tStart) = (policemanStart, thiefStart) match {
      case (Some(p), Some(t)) => (p, t)
      case _ =>
        // Find farthest nodes if start positions are not provided
        FarthestNodes.findApproximatelyFarthestNodes(originalGraph, sampleSize = 3)
    }

    // Initialize game state
    gameState = GameLogic.initializeGame(originalGraph, perturbedGraph, pStart, tStart)
    val startMessage = s"Game started with Policeman at position $pStart and Thief at position $tStart"
    logger.info(startMessage)
    startMessage
  }

  /**
   * Processes a move made by a player (Policeman or Thief).
   *
   * @param player The player making the move.
   * @param moveTo The target position of the move.
   * @return A message indicating the result of the move.
   */
  def makeMove(player: String, moveTo: Int): String = {
    // Process the move and update the game state
    gameState = GameLogic.processMove(gameState, player, moveTo)
    val (gameEnded, winner) = GameLogic.checkGameStatus(gameState)

    // Update the game state based on the move's result
    gameState = gameState.copy(gameEnded = gameEnded)
    val moveResultMessage = winner.fold("Move processed. Game continues.") { w =>
      s"Game ended. Winner: $w"
    }
    logger.info(s"Player $player made move to $moveTo: $moveResultMessage")
    moveResultMessage
  }

  /**
   * Retrieves the current game state.
   *
   * @return The current state of the game.
   */
  def getCurrentGameState: GameState = {
    logger.debug("Fetching current game state")
    gameState
  }
}
