import APIs.CustomJsonFormats
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import java.util.UUID
import scala.util.{Failure, Success}

private case class PlayerSession(id: String, playerType: String, playerId: String, inGame: Boolean, position: Option[Int] = None)
private case class MoveData(playerId: String, moveDetails: String)

object Main extends CustomJsonFormats {

  private val config = ConfigFactory.load()

  // Initialize GameLogicService and other necessary components
  private val httpInterface: String = config.getString("player-management-service.http.interface")
  private val httpPort: Int = config.getInt("player-management-service.http.port")

  private val GLSInterface: String = config.getString("player-management-service.game-logic-service.interface")
  private val GLSPort: Int = config.getInt("player-management-service.game-logic-service.port")

  private val GQSInterface: String = config.getString("player-management-service.graph-query-service.interface")
  private val GQSPort: Int = config.getInt("player-management-service.graph-query-service.port")

  // Setup logging
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val methodName = "Main"


  implicit val system: ActorSystem[_] =
    ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext: ExecutionContextExecutor =
    system.executionContext

  private var playerSessions: mutable.Map[String, PlayerSession] = mutable.Map()
  private var playerMoves: mutable.Map[String, MoveData] = mutable.Map()
  private var gameInProgress = false

  private def parseGameStartResponse(response: String): (Int, Int) = {
    // Adjust this regex pattern based on the actual response format
    val pattern = ".*Policeman at position (\\d+) and Thief at position (\\d+)".r
    response match {
      case pattern(policemanPos, thiefPos) => (policemanPos.toInt, thiefPos.toInt)
      case _ => throw new RuntimeException("Invalid game start response format")
    }
  }

  private def startNewGame(): Future[String] = {
    // Prepare POST request with an empty JSON array
    val emptyPositionsArray = JsArray()

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = s"http://$GLSInterface:$GLSPort/start-game", // Adjust the URI as needed
      entity = HttpEntity(ContentTypes.`application/json`, emptyPositionsArray.toString())
    )

    // Send request to GameLogicService and handle the response
    Http().singleRequest(request).flatMap { response =>
      response.status match {
        case StatusCodes.OK => Unmarshal(response.entity).to[String] // Reading the response entity as a String
        case _ => Future.failed(new RuntimeException("Failed to start the game"))
      }
    }.recoverWith { case ex =>
      Future.failed(new RuntimeException("Error occurred while starting the game", ex))
    }
  }

  private def handleQueryRequest(playerType: String): Future[Map[Int, Double]] = {
    val queryUri = Uri(s"http://$GQSInterface:$GQSPort/query").withQuery(Uri.Query("playerType" -> playerType))

    Http().singleRequest(HttpRequest(uri = queryUri)).flatMap { response =>
      response.status match {
        case StatusCodes.OK => Unmarshal(response.entity).to[Map[Int, Double]]
        case _ => Future.failed(new RuntimeException("Failed to query GraphQueryService"))
      }
    }
  }

  private def fetchGameStatus(): Future[String] = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(
      HttpRequest(uri = s"http://$GQSInterface:$GQSPort/game-status") // Use the correct port and route
    )

    responseFuture.flatMap { response =>
      response.status match {
        case StatusCodes.OK => Unmarshal(response.entity).to[String]
        case _ => Future.failed(new RuntimeException("Failed to fetch game status"))
      }
    }.recoverWith { case ex =>
      Future.failed(new RuntimeException("Error occurred while fetching game status", ex))
    }
  }

  // Define your routes here
  private val route: Route = concat(
    path("connect") {
      post {
        formField("playerType") { playerType =>
          formField("playerId") { playerId =>
            playerSessions.values.find(_.playerId == playerId) match {
              case Some(existingSession) =>
                complete(s"You are already connected as ${existingSession.playerType}, sessionId: ${existingSession.id}")

              case None =>
                val roleAlreadyTaken = playerSessions.exists(_._2.playerType == playerType)
                val assignedRole = if (roleAlreadyTaken) {
                  if (playerType == "Policeman") "Thief" else "Policeman"
                } else {
                  playerType
                }
                val sessionId = UUID.randomUUID().toString
                val newSession = PlayerSession(sessionId, assignedRole, playerId, inGame = true)
                playerSessions += (sessionId -> newSession)

                complete(s"Role assigned: $assignedRole, sessionId: $sessionId")
            }
          }
        }
      }
    },
    path("start-game") {
      get {
        if (!gameInProgress && playerSessions.count(_._2.inGame) >= 2) {
          onSuccess(startNewGame()) { response =>
            try {
              val (policemanPosition, thiefPosition) = parseGameStartResponse(response)

              // Update playerSessions with the new positions
              playerSessions = playerSessions.map { case (sessionId, session) =>
                val newPosition = session.playerType match {
                  case "Policeman" => Some(policemanPosition)
                  case "Thief" => Some(thiefPosition)
                  case _ => None
                }
                sessionId -> session.copy(position = newPosition)
              }

              gameInProgress = true
              complete("Game started")
            } catch {
              case ex: RuntimeException =>
                complete(StatusCodes.InternalServerError -> ex.getMessage)
            }
          }
        } else {
          complete(StatusCodes.BadRequest -> "Not enough players or game already started")
        }
      }
    },
    path("current-state") {
      post {
        formField("sessionId") { sessionId =>
          playerSessions.get(sessionId) match {
            case Some(session) if session.inGame =>
              onSuccess(fetchGameStatus()) { gameStatus =>
                if (gameStatus.startsWith("Game ended")) {
                  complete(gameStatus)
                } else {
                  onSuccess(handleQueryRequest(session.playerType)) { scores =>
                    val currentPosition = session.position.getOrElse(-1)
                    val response = s"You are the ${session.playerType} at position $currentPosition. Available moves: ${scores.mkString(", ")}"
                    complete(response)
                  }
                }
              }
            case _ =>
              complete(StatusCodes.BadRequest -> "Invalid session or player not in a game")
          }
        }
      }
    }
  )

  /**
   * Main method to start the HTTP server and bind routes.
   * The server runs until the user presses RETURN in the console.
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
