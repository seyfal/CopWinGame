package APIs

import Game.GameLogic.GameState
import spray.json._

/**
 * A trait that provides custom JSON formats for serializing and deserializing
 * specific data structures used in the game.
 *
 * It includes formats for mapping integer keys to tuples and for the game state.
 */
trait CustomJsonFormats extends DefaultJsonProtocol {

  // Custom JSON format for a map with integer keys and tuple values
  implicit object IntMapFormat extends RootJsonFormat[Map[Int, (Boolean, List[Int])]] {

    // Serializes a Map[Int, (Boolean, List[Int])] to JSON
    override def write(map: Map[Int, (Boolean, List[Int])]): JsValue = {
      JsObject(map.map { case (key, value) =>
        key.toString -> tuple2Format[Boolean, List[Int]].write(value)
      })
    }

    // Deserializes JSON to a Map[Int, (Boolean, List[Int])]
    override def read(value: JsValue): Map[Int, (Boolean, List[Int])] =
      value.asJsObject.fields.map { case (key, jsValue) =>
        key.toInt -> tuple2Format[Boolean, List[Int]].read(jsValue)
      }
  }

  // Custom JSON format for the GameState class
  implicit object GameStateFormat extends RootJsonFormat[GameState] {

    // Serializes a GameState object to JSON
    override def write(state: GameState): JsValue = {
      JsObject(
        "originalGraph" -> IntMapFormat.write(state.originalGraph),
        "perturbedGraph" -> IntMapFormat.write(state.perturbedGraph),
        "policemanPosition" -> JsNumber(state.policemanPosition),
        "thiefPosition" -> JsNumber(state.thiefPosition),
        "gameEnded" -> JsBoolean(state.gameEnded),
        "winner" -> state.winner.toJson
      )
    }

    // Deserializes JSON to a GameState object
    override def read(value: JsValue): GameState = {
      value.asJsObject.getFields(
        "originalGraph",
        "perturbedGraph",
        "policemanPosition",
        "thiefPosition",
        "gameEnded",
        "winner"
      ) match {
        case Seq(
        JsObject(orig),
        JsObject(perturb),
        JsNumber(policemanPos),
        JsNumber(thiefPos),
        JsBoolean(gameEnd),
        winner
        ) =>
          GameState(
            IntMapFormat.read(orig.toJson),
            IntMapFormat.read(perturb.toJson),
            policemanPos.toInt,
            thiefPos.toInt,
            gameEnd,
            winner.convertTo[Option[String]]
          )
        case _ => deserializationError("Game.GameState expected")
      }
    }
  }
}
