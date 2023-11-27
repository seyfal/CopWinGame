package SimilarityCalculation

/**
 * The SimRank object provides functionality to calculate similarity scores
 * between nodes in two graph representations - original and perturbed.
 */
object SimRank {

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
