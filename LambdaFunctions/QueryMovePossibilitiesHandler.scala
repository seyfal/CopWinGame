public class QueryMovePossibilitiesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final GameLogicService gameLogicService = new GameLogicService(); // Assume this service contains necessary logic
    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String playerType = request.getQueryStringParameters().get("playerType");
        GameState gameState = gameLogicService.fetchGameState(); // Fetch game state logic

        List<Integer> possibleMoves = analyzePossibleMoves(gameState, playerType);
        Map<Integer, Double> scores = calculateConfidenceScores(possibleMoves, gameState, playerType);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody(gson.toJson(scores));
        return response;
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

    // Decay factor to adjust the influence of common neighbors on the similarity score
  private val decayFactor = 0.8

  /**
   * Calculates similarity scores for neighbors of the current position in the graph.
   *
   * @param og The original graph represented as a map where the key is the node ID
   *           and the value is a tuple of (Boolean, List[Int]) indicating if the
   *           node has valuable data and a list of neighboring node IDs.
   * @param pg The perturbed graph similar in structure to the original graph.
   * @param currentPosition The current node ID from which the similarity is calculated.
   * @return A map where each key is a neighbor node ID and the value is its
   *         similarity score with respect to the original graph.
   */
  def calculateSimilarity(og: Map[Int, (Boolean, List[Int])], pg: Map[Int, (Boolean, List[Int])], currentPosition: Int): Map[Int, Double] = {
    // Retrieve the neighboring nodes of the current position in the original graph
    val currentNeighborsOG = og(currentPosition)._2

    // Calculate similarity scores for each neighbor
    currentNeighborsOG.map { neighbor =>
      // Calculate the similarity score based on common and total neighbors
      val simScore = pg.get(neighbor) match {
        case Some((_, neighborsPG)) =>
          val commonNeighbors = og(neighbor)._2.intersect(neighborsPG).size
          val totalNeighbors = og(neighbor)._2.size max 1
          commonNeighbors.toDouble / totalNeighbors // Ratio of common to total neighbors
        case None => 0.0 // No similarity if the neighbor does not exist in the perturbed graph
      }

      // Apply the decay factor to the similarity score
      neighbor -> (simScore * decayFactor)
    }.toMap
  }
}
