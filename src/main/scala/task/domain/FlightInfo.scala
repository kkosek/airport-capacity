package task.domain

case class FlightId(id: String) extends AnyVal

case class FlightInfo(id: FlightId, long: Longtitude, lat: Latitude, onGround: Boolean)
