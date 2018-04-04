package airportcapacity.service

import akka.event.LoggingAdapter
import airportcapacity.domain.{Airport, AirportId}

import scala.io.Source

case class AirportConfig(airportsIds: Seq[AirportId], filename: String)

class AirportService(config: AirportConfig, logger: LoggingAdapter) {
  def getAirports(): Seq[Airport] = {
    val lines = Source.fromResource(config.filename).getLines().toSeq
    config.airportsIds.flatMap(id => lines.find(_.contains("\"" + id.id + "\""))).flatMap(Airport(_))
  }
}
