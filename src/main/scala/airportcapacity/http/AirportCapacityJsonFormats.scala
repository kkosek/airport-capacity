package airportcapacity.http

import airportcapacity.service.OpenSkyData

object AirportCapacityJsonFormats extends AirportCapacityJsonProtocol {
  implicit val openSkyDataFormat = jsonFormat2(OpenSkyData.apply)
}
