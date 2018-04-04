package task.service

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import task.domain.{Airport, AirportId, FlightInfo}

import scala.concurrent.{ExecutionContext, Future}

case class FlightInfoConfig(openSkyURL: String, openFlightURL: String, airportIds: Seq[AirportId])

class FlightInfoService(logger: LoggingAdapter, config: FlightInfoConfig)(implicit ec: ExecutionContext, mat: Materializer, system: ActorSystem) {
  def getStates(): Future[Seq[FlightInfo]] = {
    for {
      response <- Http().singleRequest(HttpRequest(method = HttpMethods.GET, uri = config.openSkyURL))
      entity <- Unmarshal(response.entity).to[Seq[FlightInfo]]
    } yield entity
  }
}
