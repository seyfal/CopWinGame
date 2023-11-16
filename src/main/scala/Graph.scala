import scala.collection.mutable
import scala.util.Random

object Graph {
  private type Graph = Map[Int, (Boolean, List[Int])]

  private def dijkstra(graph: Graph, start: Int): Map[Int, Int] = {
    val distances = mutable.Map[Int, Int](graph.keys.map(_ -> Int.MaxValue).toSeq: _*)
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

  def findApproximatelyFarthestNodes(graph: Graph, sampleSize: Int): (Int, Int) = {
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
}
