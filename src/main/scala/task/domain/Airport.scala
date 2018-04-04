package task.domain

case class AirportId(id: String)

case class Longtitude(long: Double)

object Longtitude {
  def apply(long: String): Longtitude = Longtitude(long.dropRight(1).drop(1).toDouble)
}

case class Latitude(lat: Double)

object Latitude {
  def apply(lat: String): Latitude = Latitude(lat.dropRight(1).drop(1).toDouble)
}

case class Airport(id: AirportId, long: Longtitude, lat: Latitude)

object Airport {
  def apply(csvString: String): Option[Airport] =
    csvString.split(",") match {
      case Array(_, _, _, _, airportId, _, long, lat, _*) => Option(Airport(AirportId(airportId.drop(1).dropRight(1)), Longtitude(long), Latitude(lat)))
      case _ => None
    }
}
