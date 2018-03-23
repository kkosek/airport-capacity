package task.domain

case class AirportId(id: String) extends AnyVal

case class Longtitude(long: BigDecimal) extends AnyVal

case class Latitude(lat: BigDecimal) extends AnyVal

case class Airport(id: AirportId, long: Longtitude, lat: Latitude)
