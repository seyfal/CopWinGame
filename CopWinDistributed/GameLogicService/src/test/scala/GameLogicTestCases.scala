import Game.GameLogic
import org.scalatest.funsuite.AnyFunSuite

class GameLogicTestCases extends AnyFunSuite {

  // Example graph for testing
  val testGraph: Map[Int, (Boolean, List[Int])] = Map(
    1 -> (false, List(2, 3)),
    2 -> (true, List(1)), // Node 2 has valuable data
    3 -> (false, List(1, 2)),
    4 -> (false, List())
  )

  test("Game initializes correctly") {
    val gameState = GameLogic.initializeGame(testGraph, testGraph, 1, 3)
    assert(gameState.policemanPosition == 1)
    assert(gameState.thiefPosition == 3)
    assert(!gameState.gameEnded)
  }

  test(
    "Processing valid and invalid moves, Thief moves to a node with valuable data"
  ) {
    var gameState = GameLogic.initializeGame(testGraph, testGraph, 1, 3)
    // Valid move
    gameState = GameLogic.processMove(gameState, "Thief", 2)
    assert(gameState.thiefPosition == 2)
    assert(gameState.gameEnded)
  }

  test(
    "Processing valid and invalid moves, Thief moves to a node that does not exist"
  ) {
    var gameState = GameLogic.initializeGame(testGraph, testGraph, 1, 3)
    // Invalid move (to a non-existent node)
    gameState = GameLogic.processMove(gameState, "Thief", 5)
    assert(gameState.gameEnded)
  }

  test(
    "Processing valid and invalid moves, Policeman moves to a node that does not exist"
  ) {
    var gameState = GameLogic.initializeGame(testGraph, testGraph, 1, 3)
    // Invalid move (to a non-existent node)
    gameState = GameLogic.processMove(gameState, "Policeman", 5)
    assert(gameState.gameEnded)
  }

  test(
    "Processing valid and invalid moves, Thief moves to a node that is unreachable from the current node"
  ) {
    var gameState = GameLogic.initializeGame(testGraph, testGraph, 1, 3)
    // Invalid move (to a non-existent node)
    gameState = GameLogic.processMove(gameState, "Thief", 4)
    assert(gameState.gameEnded)
  }

  test(
    "Processing valid and invalid moves, Policeman moves to a node that is unreachable from the current node"
  ) {
    var gameState = GameLogic.initializeGame(testGraph, testGraph, 1, 3)
    // Invalid move (to a non-existent node)
    gameState = GameLogic.processMove(gameState, "Policeman", 4)
    assert(gameState.gameEnded)
  }

  test(
    "Checking game status for win/loss conditions, Thief gets to the valuable node"
  ) {
    // Thief wins by reaching node with valuable data
    var gameState = GameLogic.initializeGame(testGraph, testGraph, 1, 3)
    gameState = GameLogic.processMove(gameState, "Thief", 2)
    val (gameEnded, winner) = GameLogic.checkGameStatus(gameState)
    assert(gameEnded && winner.contains("Thief"))
  }

  test(
    "Checking game status for win/loss conditions, Policeman catches the Thief"
  ) {
    // Policeman wins by catching the Thief
    var gameState = GameLogic.initializeGame(testGraph, testGraph, 1, 3)
    gameState = GameLogic.processMove(gameState, "Policeman", 3)
    val (gameEnded2, winner2) = GameLogic.checkGameStatus(gameState)
    assert(gameEnded2 && winner2.contains("Policeman"))
  }

  test(
    "Checking game status for win/loss conditions, Thief gets to the valuable node and then the Policeman catches the Thief"
  ) {
    // Policeman wins by catching the Thief
    var gameState = GameLogic.initializeGame(testGraph, testGraph, 1, 3)
    gameState = GameLogic.processMove(gameState, "Thief", 2)
    gameState = GameLogic.processMove(gameState, "Policeman", 2)
    val (gameEnded2, winner2) = GameLogic.checkGameStatus(gameState)
    assert(gameEnded2 && winner2.contains("Thief"))
  }

  test(
    "Checking game status for win/loss conditions, Policeman catches the Thief and then the Thief gets to the valuable node"
  ) {
    // Policeman wins by catching the Thief
    var gameState = GameLogic.initializeGame(testGraph, testGraph, 1, 3)
    gameState = GameLogic.processMove(gameState, "Policeman", 2)
    gameState = GameLogic.processMove(gameState, "Thief", 2)
    val (gameEnded2, winner2) = GameLogic.checkGameStatus(gameState)
    assert(gameEnded2 && winner2.contains("Policeman"))
  }

  test(
    "Checking game status for win/loss conditions, Policeman moves to a valid node and the Thief moves to an unreachable node"
  ) {
    // Policeman wins by catching the Thief
    var gameState = GameLogic.initializeGame(testGraph, testGraph, 1, 3)
    gameState = GameLogic.processMove(gameState, "Policeman", 2)
    gameState = GameLogic.processMove(gameState, "Thief", 4)
    val (gameEnded2, winner2) = GameLogic.checkGameStatus(gameState)
    assert(gameEnded2 && winner2.contains("Policeman"))
  }

  test(
    "Checking game status for win/loss conditions, Thief moves to a valid node and the Policeman moves to an unreachable node"
  ) {
    // Policeman wins by catching the Thief
    var gameState = GameLogic.initializeGame(testGraph, testGraph, 1, 3)
    gameState = GameLogic.processMove(gameState, "Thief", 3)
    gameState = GameLogic.processMove(gameState, "Thief", 4)
    val (gameEnded2, winner2) = GameLogic.checkGameStatus(gameState)
    assert(gameEnded2 && winner2.contains("Policeman"))
  }
}
