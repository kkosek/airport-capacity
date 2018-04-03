package task.service

import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import task.domain.{Airport, AirportId, FlightInfo}

import scala.concurrent.{ExecutionContext, Future}

case class FlightInfoConfig(openSkyURL: String, openFlightURL: String, airportIds: Seq[AirportId])

class FlightInfoService(logger: LoggingAdapter, config: FlightInfoConfig)(implicit ec: ExecutionContext, mat: Materializer) {
  def getAirports(): Future[Seq[Airport]] = ???
  def getStates(): Future[Seq[FlightInfo]] = ???

  private def get[A](url: String): Future[Seq[A]] =
    for {
      response <- Http().singleRequest(HttpRequest(method = HttpMethods.GET, uri = url))
      entity   <- Unmarshal(response.entity).to[Seq[A]]
    } yield entity
}
