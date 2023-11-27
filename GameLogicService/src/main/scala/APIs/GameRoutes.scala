package APIs

import com.typesafe.config.ConfigFactory

import Game.GameState
import LoadPhase.LoadMethods
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.slf4j.LoggerFactory
import spray.json._

/**
 * Defines the HTTP routes for the Game Logic Service.
 *
 * This trait contains the routes to handle HTTP requests related to game state management,
 * including starting a new game and making moves.
 */
trait GameRoutes extends CustomJsonFormats {

  // Setup logging
  private val logger = LoggerFactory.getLogger(this.getClass)

  // Load configuration for graph file paths
  private val config = ConfigFactory.load()
  private val originalNodesPath = config.getString("game-logic-service.graph.originalNodes")
  private val originalEdgesPath = config.getString("game-logic-service.graph.originalEdges")
  private val perturbedNodesPath = config.getString("game-logic-service.graph.perturbedNodes")
  private val perturbedEdgesPath = config.getString("game-logic-service.graph.perturbedEdges")

  // Load original and perturbed graphs using paths from config file
  val originalGraph: Map[Int, (Boolean, List[Int])] = LoadMethods.createGraph(
    originalNodesPath,
    originalEdgesPath
  )
  val perturbedGraph: Map[Int, (Boolean, List[Int])] = LoadMethods.createGraph(
    perturbedNodesPath,
    perturbedEdgesPath
  )

  // Inject the GameLogicService from the main
  val gameLogicService: GameLogicService

  // Implicit JSON format for GameState case class
  implicit val gameStateFormat: RootJsonFormat[GameState] = jsonFormat6(GameState)

  // Define the HTTP routes for the service
  val route: Route =
    concat(
      post {
        path("game-state") {
          entity(as[GameState]) { gameState =>
            logger.info("Received game state: {}", gameState)
            complete(StatusCodes.OK, "Game state processed")
          }
        }
      },
      post {
        path("start-game") {
          entity(as[JsArray]) { jsonArray =>
            val positions = jsonArray.elements.map {
              case JsNumber(num) => Some(num.toInt)
              case _ => None
            }
            val response = gameLogicService.startNewGame(
              originalGraph,
              perturbedGraph,
              positions.headOption.flatten,
              positions.lastOption.flatten
            )
            logger.info("Starting new game with response: {}", response)
            complete(StatusCodes.OK, response)
          }
        }
      },
      post {
        path("make-move") {
          entity(as[(String, Int)]) { case (player, moveTo) =>
            val response = gameLogicService.makeMove(player, moveTo)
            logger.debug("Processed move for player {} to position {}. Response: {}", player, moveTo, response)
            complete(StatusCodes.OK, response)
          }
        }
      },
      get {
        path("game-state") {
          val gameState = gameLogicService.getCurrentGameState
          logger.debug("Current game state: {}", gameState)
          complete(gameState)
        }
      }
    )
}
