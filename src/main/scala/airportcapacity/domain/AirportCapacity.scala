package airportcapacity.domain

import java.time.{LocalTime, ZoneId}

case class FlightsArrived(number: Int) extends AnyVal

case class FlightsDepartured(number: Int) extends AnyVal

case class Time(time: LocalTime, zone: ZoneId)

case class AirportCapacity(id: String, time: String, arrived: Int, departured: Int)
