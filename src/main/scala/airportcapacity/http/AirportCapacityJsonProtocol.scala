package airportcapacity.http

import airportcapacity.domain.{FlightId, FlightInfo, Latitude, Longtitude}
import spray.json._

trait AirportCapacityJsonProtocol extends DefaultJsonProtocol {
  implicit val flightInfoJsonFormat = new RootJsonFormat[FlightInfo] {
    override def write(obj: FlightInfo): JsValue = ???
    override def read(json: JsValue): FlightInfo = json match {
      case JsArray(Vector(JsString(id), _, _, _, _, JsNumber(long), JsNumber(lat), _, JsBoolean(onGround), _*)) =>
        FlightInfo(FlightId(id), Longtitude(long.toDouble), Latitude(lat.toDouble), onGround)
      case _ =>
        deserializationError(s"Couldn't parse state json array: $json")
    }
  }
}

object AirportCapacityJsonProtocol extends AirportCapacityJsonProtocol
