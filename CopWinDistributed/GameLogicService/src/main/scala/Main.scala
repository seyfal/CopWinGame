import APIs.{CustomJsonFormats, GameLogicService, GameRoutes}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

/** The main object for the Game Logic Service.
  *
  * This object initializes and starts an Akka HTTP server that listens for requests
  * and handles them using routes defined in the GameRoutes trait.
  */
object Main extends GameRoutes with CustomJsonFormats {

  private val config = ConfigFactory.load()

  // Initialize GameLogicService and other necessary components
  override val gameLogicService = new GameLogicService()
  private val httpInterface: String = config.getString("game-logic-service.http.interface")
  private val httpPort: Int = config.getInt("game-logic-service.http.port")

  // Setup logging
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val methodName = "Main"

  // Initialize Actor System for Akka
  implicit val system: ActorSystem[_] =
    ActorSystem(Behaviors.empty, "GameLogicService")
  implicit val executionContext: ExecutionContextExecutor =
    system.executionContext

  /** The main entry point for the application.
    *
    * Starts the HTTP server on the specified interface and port. The server
    * remains active until a user inputs a newline character in the console.
    *
    * @param args command-line arguments (not used).
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
