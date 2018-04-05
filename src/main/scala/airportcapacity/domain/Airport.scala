package airportcapacity.domain

import airportcapacity.utils.StringExtensions._

case class AirportId(id: String)

case class Position(long: Double, lat: Double)

object Position {
  def apply(long: String, lat: String): Position = Position(long.removeDoubleQuotesIfExist.toDouble, lat.removeDoubleQuotesIfExist.toDouble)
}

case class Airport(id: AirportId, position: Position)

object Airport {
  def apply(csvString: String): Option[Airport] =
    csvString.split(",") match {
      case Array(_, _, _, _, airportId, _, long, lat, _*) => Option(Airport(AirportId(airportId.removeDoubleQuotesIfExist), Position(long, lat)))
      case _                                              => None
    }
}
