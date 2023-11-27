package APIs

import Game.GameState
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json._

class GameRoutesTestCases
    extends AnyWordSpec
    with Matchers
    with ScalatestRouteTest
    with GameRoutes {
  override val gameLogicService =
    new GameLogicService() // Mock or actual service

  "GameRoutes" should {
    "respond to the /game-state POST endpoint" in {
      val testGameState = GameState(
        Map(
          1 -> (true, List(2, 3)),
          2 -> (false, List(1, 3)),
          3 -> (false, List(1, 2))
        ),
        Map(
          1 -> (true, List(2, 3)),
          2 -> (false, List(1, 3)),
          3 -> (false, List(1, 2))
        ),
        1,
        2,
        gameEnded = false,
        winner = None
      )
      val request = HttpRequest(
        method = HttpMethods.POST,
        uri = "/game-state",
        entity = HttpEntity(
          ContentTypes.`application/json`,
          testGameState.toJson.toString
        )
      )

      request ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual "Game state processed"
      }
    }

    "respond to the /start-game POST endpoint" in {
      val request = HttpRequest(
        method = HttpMethods.POST,
        uri = "/start-game",
        entity = HttpEntity(
          ContentTypes.`application/json`,
          """[1, 2]""" // This represents Some(1), Some(2)
        )
      )

      request ~> route ~> check {
        status shouldBe StatusCodes.OK
        // Additional assertions based on expected response
      }
    }

    "respond to the /make-move POST endpoint" in {
      val request = HttpRequest(
        method = HttpMethods.POST,
        uri = "/make-move",
        entity = HttpEntity(
          ContentTypes.`application/json`,
          """["Thief", 3]"""  // Correct JSON format for Tuple2[String, Int]
        )
      )

      request ~> route ~> check {
        status shouldBe StatusCodes.OK
        // Additional assertions based on expected response
      }
    }

    "respond to the /game-state GET endpoint" in {
      Get("/game-state") ~> route ~> check {
        status shouldBe StatusCodes.OK
        // Assertions about the response content
        // e.g., responseAs[GameState] shouldEqual expectedGameState
      }
    }
  }
}
