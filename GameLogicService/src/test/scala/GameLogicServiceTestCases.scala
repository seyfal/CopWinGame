import APIs.GameLogicService
import org.scalatest.funsuite.AnyFunSuite

class GameLogicServiceTestCases extends AnyFunSuite {

  // Example graph for testing
  val testGraph: Map[Int, (Boolean, List[Int])] = Map(
    1 -> (false, List(2, 3)),
    2 -> (true, List(1)), // Node 2 has valuable data
    3 -> (false, List(1, 2)),
    4 -> (false, List(2))
  )

  test("Game starts correctly with APIs.GameLogicService") {
    val gameLogicService = new GameLogicService()
    val startMessage = gameLogicService.startNewGame(testGraph, testGraph, Option(1), Option(3))
    assert(
      startMessage.contains(
        "Game started with Policeman at position"
      ) && startMessage.contains("and Thief at position")
    )
  }

  test("APIs.GameLogicService processes moves correctly") {
    val gameLogicService = new GameLogicService()
    gameLogicService.startNewGame(testGraph, testGraph, Option(1), Option(3))

    val moveResponse = gameLogicService.makeMove("Thief", 2)
    assert(
      moveResponse.contains("Game ended. Winner: Thief") || moveResponse
        .contains("Move processed. Game continues.")
    )

    val currentState = gameLogicService.getCurrentGameState
    assert(currentState.thiefPosition == 2)
  }

  test(
    "APIs.GameLogicService processes moves correctly, Policeman gets to the valuable data first" +
      " and then Thief makes a move to the same node"
  ) {
    val gameLogicService = new GameLogicService()
    val startMessage = gameLogicService.startNewGame(testGraph, testGraph, Option(1), Option(3))

    // Assuming startMessage is formatted like "Game started with Policeman at position X and Thief at position Y"
    // Extract initial positions
    val positionPattern =
      """.*Policeman at position (\d+) and Thief at position (\d+).*""".r
    val positionPattern(initialPolicemanPosition, initialThiefPosition) =
      startMessage

    // Convert positions to Int
    val policemanPos = initialPolicemanPosition.toInt
    val thiefPos = initialThiefPosition.toInt

    println(
      s"Initial positions: Policeman at $policemanPos, Thief at $thiefPos"
    )

    // Validate initial positions
    assert(
      startMessage.contains(
        s"Game started with Policeman at position $policemanPos"
      )
    )
    assert(startMessage.contains(s"and Thief at position $thiefPos"))

    val moveResponse_1 = gameLogicService.makeMove("Policeman", 2)
    assert(moveResponse_1.contains("Move processed. Game continues."))

    val moveResponse_2 = gameLogicService.makeMove("Thief", 2)
    assert(moveResponse_2.contains("Game ended. Winner: Policeman"))

    val currentState = gameLogicService.getCurrentGameState
    assert(currentState.thiefPosition == 2)
    assert(currentState.policemanPosition == 2)
    assert(currentState.gameEnded)
    assert(currentState.winner.contains("Policeman"))
  }

  test("APIs.GameLogicService returns the correct current game state") {
    val gameLogicService = new GameLogicService()
    val startMessage = gameLogicService.startNewGame(testGraph, testGraph, Option(1), Option(3))

    // Assuming startMessage is formatted like "Game started with Policeman at position X and Thief at position Y"
    // Extract initial positions
    val positionPattern =
      """.*Policeman at position (\d+) and Thief at position (\d+).*""".r
    val positionPattern(initialPolicemanPosition, initialThiefPosition) =
      startMessage

    // Convert positions to Int
    val policemanPos = initialPolicemanPosition.toInt
    val thiefPos = initialThiefPosition.toInt

    // Validate initial positions
    assert(
      startMessage.contains(
        s"Game started with Policeman at position $policemanPos"
      )
    )
    assert(startMessage.contains(s"and Thief at position $thiefPos"))

    // Make a move and then check the updated game state
    gameLogicService.makeMove("Thief", 2)

    val currentState = gameLogicService.getCurrentGameState
    assert(
      currentState.thiefPosition == 2
    ) // Ensure Thief's new position is as expected
    assert(currentState.gameEnded) // Check if the game has ended
  }

  test(
    "APIs.GameLogicService processes moves correctly, Policeman gets to the valuable data first" +
      " and then Thief makes an invalid move to an unreachable node"
  ) {
    val gameLogicService = new GameLogicService()
    val startMessage = gameLogicService.startNewGame(testGraph, testGraph, Option(3), Option(1))

    // Assuming startMessage is formatted like "Game started with Policeman at position X and Thief at position Y"
    // Extract initial positions
    val positionPattern =
      """.*Policeman at position (\d+) and Thief at position (\d+).*""".r
    val positionPattern(initialPolicemanPosition, initialThiefPosition) =
      startMessage

    // Convert positions to Int
    val policemanPos = initialPolicemanPosition.toInt
    val thiefPos = initialThiefPosition.toInt

    println(
      s"Initial positions: Policeman at $policemanPos, Thief at $thiefPos"
    )

    // Validate initial positions
    assert(
      startMessage.contains(
        s"Game started with Policeman at position $policemanPos"
      )
    )
    assert(startMessage.contains(s"and Thief at position $thiefPos"))

    val moveResponse_1 = gameLogicService.makeMove("Policeman", 2)
    assert(moveResponse_1.contains("Move processed. Game continues."))

    val moveResponse_2 = gameLogicService.makeMove("Thief", 4)
    assert(moveResponse_2.contains("Game ended. Winner: Policeman"))

    val currentState = gameLogicService.getCurrentGameState
    assert(currentState.thiefPosition == 1)
    assert(currentState.policemanPosition == 2)
    assert(currentState.gameEnded)
    assert(currentState.winner.contains("Policeman"))
  }
}
