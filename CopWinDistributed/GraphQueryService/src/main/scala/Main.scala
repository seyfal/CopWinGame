import APIs.CustomJsonFormats
import Game.GameState
import SimilarityCalculation.SimRank.calculateSimilarity
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

/**
 * Main object for the Graph Query Service.
 *
 * This object initializes and runs an HTTP server to handle queries related to
 * game state and move possibilities.
 */
object Main extends CustomJsonFormats {

  private val config = ConfigFactory.load()

  // Initialize GameLogicService and other necessary components
  private val httpInterface: String = config.getString("player-management-service.http.interface")
  private val httpPort: Int = config.getInt("player-management-service.http.port")

  private val GLSInterface: String = config.getString("player-management-service.game-logic-service.interface")
  private val GLSPort: Int = config.getInt("player-management-service.game-logic-service.port")

  // Setup logging
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val methodName = "Main"

  // Initialize the Actor System for managing actors
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  /**
   * Fetches the current game state from a predefined URI.
   *
   * @return A future containing the game state or an error.
   */
  private def fetchGameState: Future[GameState] = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"http://$GLSInterface:$GLSPort/game-state"))
    responseFuture.flatMap { response =>
      response.status match {
        case StatusCodes.OK => Unmarshal(response.entity).to[GameState]
        case _ => Future.failed(new RuntimeException("Unexpected response status"))
      }
    }.recoverWith { case ex => Future.failed(new RuntimeException("Error occurred while fetching GameState", ex)) }
  }

  /**
   * Analyzes possible moves for a player based on the current game state.
   *
   * @param gameState The current state of the game.
   * @param playerType The type of player (e.g., "policeman", "thief").
   * @return A list of possible moves (node IDs).
   */
  private def analyzePossibleMoves(gameState: GameState, playerType: String): List[Int] = {
    val currentPosition = playerType.toLowerCase match {
      case "policeman" => gameState.policemanPosition
      case "thief" => gameState.thiefPosition
    }
    gameState.originalGraph.get(currentPosition).map(_._2).getOrElse(List.empty)
  }

  /**
   * Calculates confidence scores for potential moves based on similarity calculations.
   *
   * @param moves A list of potential moves.
   * @param gameState The current game state.
   * @param playerType The type of player (e.g., "policeman", "thief").
   * @return A map of moves and their corresponding confidence scores.
   */
  private def calculateConfidenceScores(moves: List[Int], gameState: GameState, playerType: String): Map[Int, Double] = {
    val currentPosition = playerType.toLowerCase match {
      case "policeman" => gameState.policemanPosition
      case "thief" => gameState.thiefPosition
    }
    val similarityScores = calculateSimilarity(gameState.originalGraph, gameState.perturbedGraph, currentPosition)
    moves.map(move => move -> similarityScores.getOrElse(move, 0.0)).toMap
  }

  /**
   * Handles an HTTP request to query potential moves and their confidence scores.
   *
   * @param playerType The type of player making the query.
   * @return A future containing the response to be sent back to the client.
   */
  private def handleQueryRequest(playerType: String): Future[Map[Int, Double]] = {
    fetchGameState.flatMap { gameState =>
      val possibleMoves = analyzePossibleMoves(gameState, playerType)
      Future.successful(calculateConfidenceScores(possibleMoves, gameState, playerType))
    }
  }

  // Defines the route for querying move possibilities and confidence scores
  private val queryRoute: Route = path("query") {
    parameters("playerType") { playerType =>
      onComplete(handleQueryRequest(playerType)) {
        case Success(scores) => complete(scores)
        case Failure(ex) => complete(StatusCodes.InternalServerError -> ex.getMessage)
      }
    }
  }

  /**
   * Determines the status of the game including if it has ended and the winner.
   *
   * @param gameState The current state of the game.
   * @return A string representing the game status.
   */
  private def getGameStatus(gameState: GameState): String = {
    if (gameState.gameEnded) {
      gameState.winner.fold("Game ended. No winner.")(winner => s"Game ended. Winner: $winner")
    } else {
      "Game is ongoing."
    }
  }

  // Defines the route for querying the current game status
  private val gameStatusRoute: Route = path("game-status") {
    onComplete(fetchGameState) {
      case Success(gameState) =>
        val status = getGameStatus(gameState)
        complete(status)
      case Failure(ex) =>
        complete(StatusCodes.InternalServerError -> ex.getMessage)
    }
  }

  // Combine the routes for handling different types of requests
  private val route: Route = queryRoute ~ gameStatusRoute

  /**
   * Main method to start the server and handle HTTP requests.
   *
   * @param args Command-line arguments (not used).
   */
  def main(args: Array[String]): Unit = {
    // Start HTTP server with configured interface and port
    val bindingFuture = Http().newServerAt(httpInterface, httpPort).bind(route)
    logger.info(
      s"$methodName: Server online at http://$httpInterface:$httpPort/\nPress RETURN to stop..."
    )

    // Shutdown server when user presses RETURN
    StdIn.readLine()
    // Trigger unbinding from the port and shutdown when done
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
