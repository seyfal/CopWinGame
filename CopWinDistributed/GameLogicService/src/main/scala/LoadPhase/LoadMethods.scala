package LoadPhase

import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.io.Source

/** Provides methods for loading nodes and edges from JSON and text files
  * to create a graph representation.
  */
object LoadMethods {

  /** Creates a graph from node and edge files.
    *
    * The graph is represented as a map where each key is a node ID and
    * the value is a tuple containing a boolean for 'valuableData' and a list of adjacent nodes.
    *
    * @param nodePath Path to the JSON file containing node data.
    * @param edgePath Path to the text file containing edge data.
    * @return A map representing the graph.
    */
  def createGraph(
      nodePath: String,
      edgePath: String
  ): Map[Int, (Boolean, List[Int])] = {
    val nodes = loadNodes(nodePath)
    val edges = loadEdges(edgePath)

    nodes.map { case (id, properties) =>
      (id, (properties.valuableData, edges.getOrElse(id, List())))
    }
  }

  implicit val formats: DefaultFormats.type = DefaultFormats

  /** Loads nodes from a JSON file.
    *
    * Each line in the file should be a JSON object representing a node with an 'id' and 'valuableData' fields.
    *
    * @param nodePath Path to the JSON file containing node data.
    * @return A map of node IDs to their properties.
    */
  private def loadNodes(nodePath: String): Map[Int, NodeProperties] = {
    val fileContent = Source.fromFile(nodePath).getLines()
    fileContent.map { line =>
      val json = parse(line)
      val id = (json \ "id").extract[Int]
      val valuableData = (json \ "valuableData").extract[Boolean]
      (id, NodeProperties(id, valuableData))
    }.toMap
  }

  /** Loads edges from a text file and creates an adjacency list.
    *
    * Each line in the file should represent an edge with two integers: a source and a destination node ID.
    *
    * @param edgePath Path to the text file containing edge data.
    * @return A map representing the adjacency list of the graph.
    */
  private def loadEdges(edgePath: String): Map[Int, List[Int]] = {
    val fileContent = Source.fromFile(edgePath).getLines()
    fileContent.foldLeft(Map.empty[Int, List[Int]]) { (acc, line) =>
      val parts = line.split(" ")
      val from = parts(0).toInt
      val to = parts(1).toInt

      acc + (from -> (acc.getOrElse(from, List()) :+ to))
    }
  }

  private case class NodeProperties(id: Int, valuableData: Boolean)
}
