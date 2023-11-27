package LoadPhase

import scala.collection.mutable
import scala.util.Random

/** Provides methods to find the farthest nodes in a given graph.
  * The graph is assumed to be unweighted for these calculations.
  */
object FarthestNodes {
  private type Graph = Map[Int, (Boolean, List[Int])]

  /** Finds a pair of nodes that are approximately the farthest apart in the graph.
    * This is achieved by running Dijkstra's algorithm from a sample of nodes.
    *
    * @param graph The graph represented as a Map.
    * @param sampleSize The number of nodes to sample for finding the farthest pair.
    * @return A tuple containing the IDs of the two nodes that are approximately the farthest apart.
    */
  def findApproximatelyFarthestNodes(
      graph: Graph,
      sampleSize: Int
  ): (Int, Int) = {
    val nodes = graph.keys.toList
    val randomNodes = Random.shuffle(nodes).take(sampleSize)
    var maxDistance = 0
    var farthestPair = (0, 0)

    randomNodes.foreach { node =>
      val distances = dijkstra(graph, node)
      distances.foreach { case (target, dist) =>
        if (dist > maxDistance) {
          maxDistance = dist
          farthestPair = (node, target)
        }
      }
    }

    farthestPair
  }

  /** Implements Dijkstra's algorithm to find the shortest path distances from a start node to all other nodes in the graph.
    *
    * @param graph The graph represented as a Map. Each key is a node ID and the value is a tuple with a boolean and a list of adjacent nodes.
    * @param start The starting node ID for the shortest path calculation.
    * @return A Map where each key is a node ID and the value is the shortest distance from the start node to this node.
    */
  private def dijkstra(graph: Graph, start: Int): Map[Int, Int] = {
    val distances =
      mutable.Map[Int, Int](graph.keys.map(_ -> Int.MaxValue).toSeq: _*)
    val pq = mutable.PriorityQueue[(Int, Int)]()(Ordering.by(-_._2))

    distances(start) = 0
    pq.enqueue((start, 0))

    while (pq.nonEmpty) {
      val (currentNode, currentDist) = pq.dequeue()
      if (currentDist <= distances(currentNode)) {
        graph(currentNode)._2.foreach { neighbor =>
          val newDist = currentDist + 1 // assuming each edge has a weight of 1
          if (newDist < distances(neighbor)) {
            distances(neighbor) = newDist
            pq.enqueue((neighbor, newDist))
          }
        }
      }
    }

    distances.toMap
  }
}
