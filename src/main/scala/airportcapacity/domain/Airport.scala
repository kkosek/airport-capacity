package airportcapacity.domain

import airportcapacity.utils.StringExtensions._

case class AirportId(id: String)

//0.01 is ~1km
case class Position(long: Double, lat: Double) {
  def isNear(other: Position) =
    long - other.long < 0.01 && lat - other.lat < 0.01
}

object Position {
  def apply(long: String, lat: String): Position =
    Position(long.removeDoubleQuotesIfExist.toDouble,
             lat.removeDoubleQuotesIfExist.toDouble)
}

case class Airport(id: AirportId, position: Position)

object Airport {
  def apply(csvString: String): Option[Airport] =
    csvString.split(",") match {
      case Array(_, _, _, _, airportId, _, long, lat, _*) =>
        Option(
          Airport(AirportId(airportId.removeDoubleQuotesIfExist),
                  Position(long, lat)))
      case _ => None
    }
}
