package APIs

import Game.GameState
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

  /**
   * Custom JSON formatter for maps where the key is an integer and the value is a double.
   * This formatter handles the conversion of such maps to and from JSON.
   */
  implicit object IntMapJsonFormat extends RootJsonFormat[Map[Int, Double]] {

    /**
     * Serializes a map with integer keys and double values to JSON.
     * Each key-value pair in the map is converted into a JSON object
     * with the key as a string and the value as a JSON number.
     *
     * @param map The map to serialize.
     * @return A JsObject representing the serialized map.
     */
    def write(map: Map[Int, Double]): JsValue = {
      JsObject(map.map { case (k, v) => k.toString -> JsNumber(v) })
    }

    /**
     * Deserializes a JSON object to a map with integer keys and double values.
     * Each field in the JSON object is expected to have a string key
     * that can be converted to an integer and a JSON number as value.
     *
     * @param json The JSON to deserialize.
     * @return A map of integers to doubles.
     * @throws DeserializationException If the JSON structure does not match the expected format.
     */
    def read(json: JsValue): Map[Int, Double] = {
      json.asJsObject.fields.map { case (k, v) =>
        k.toInt -> v.convertTo[Double]
      }
    }
  }


}
