package Game

/**
 * Represents the state of a game at any given moment.
 * This state includes both the original and perturbed graphs, the positions of the players, and game status.
 *
 * @param originalGraph The original graph of the game represented as a Map.
 *                      Each key is a node ID, and its value is a tuple containing:
 *                      - A Boolean indicating if the node contains valuable data.
 *                      - A List of Integers representing adjacent node IDs.
 *
 * @param perturbedGraph A perturbed version of the original graph, structured similarly.
 *                       It represents the graph as perceived by the players with potential alterations.
 *
 * @param policemanPosition The current position (node ID) of the policeman in the game.
 *
 * @param thiefPosition The current position (node ID) of the thief in the game.
 *
 * @param gameEnded A Boolean flag indicating whether the game has ended (true) or is still ongoing (false).
 *
 * @param winner An Option[String] that holds the winner's role ("policeman" or "thief") if the game has ended.
 *               If the game is ongoing or if there is no winner yet, this will be None.
 */
final case class GameState(
                            originalGraph: Map[Int, (Boolean, List[Int])],
                            perturbedGraph: Map[Int, (Boolean, List[Int])],
                            policemanPosition: Int,
                            thiefPosition: Int,
                            gameEnded: Boolean,
                            winner: Option[String] = None
                          )
