package airportcapacity.http

import airportcapacity.domain
import airportcapacity.domain.{FlightInfo, Position}
import spray.json._

trait AirportCapacityJsonProtocol extends DefaultJsonProtocol {
  implicit val flightInfoJsonFormat = new RootJsonFormat[FlightInfo] {
    override def write(obj: FlightInfo): JsValue = ???
    override def read(json: JsValue): FlightInfo = {
      json match {
        case JsArray(Vector(JsString(id), _, _, _, _, JsNumber(long), JsNumber(lat), _, JsBoolean(onGround), _*)) =>
          FlightInfo(id, Option(Position(long.toDouble, lat.toDouble)), onGround)
        case JsArray(Vector(JsString(id), _, _, _, _, _, _, _, JsBoolean(onGround), _*)) =>
          FlightInfo(id, None, onGround)
        case _ =>
          deserializationError(s"Couldn't parse state json array: $json")
      }
    }
  }
}

object AirportCapacityJsonProtocol extends AirportCapacityJsonProtocol
