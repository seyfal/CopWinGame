import scala.io.Source
import scala.collection.mutable.{Map => MutableMap}
import org.json4s._
import org.json4s.jackson.JsonMethods._
object LoadMethods {

  case class NodeProperties(id: Int, valuableData: Boolean)

  implicit val formats: DefaultFormats.type = DefaultFormats

  // Method to load nodes from a JSON file
  def loadNodes(nodePath: String): Map[Int, NodeProperties] = {
    val fileContent = Source.fromFile(nodePath).getLines()
    fileContent.map { line =>
      val json = parse(line)
      val id = (json \ "id").extract[Int]
      val valuableData = (json \ "valuableData").extract[Boolean]
      (id, NodeProperties(id, valuableData))
    }.toMap
  }

  // Method to load edges and create adjacency list
  def loadEdges(edgePath: String): Map[Int, List[Int]] = {
    val fileContent = Source.fromFile(edgePath).getLines()
    fileContent.foldLeft(Map.empty[Int, List[Int]]) { (acc, line) =>
      val parts = line.split(" ")
      val from = parts(0).toInt
      val to = parts(1).toInt

      acc + (from -> (acc.getOrElse(from, List()) :+ to))
    }
  }

  // Create a graph from node and edge files
  def createGraph(nodePath: String, edgePath: String): Map[Int, (Boolean, List[Int])] = {
    val nodes = loadNodes(nodePath)
    val edges = loadEdges(edgePath)

    nodes.map { case (id, properties) =>
      (id, (properties.valuableData, edges.getOrElse(id, List())))
    }
  }
}
