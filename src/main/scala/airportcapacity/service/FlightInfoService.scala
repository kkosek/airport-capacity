package airportcapacity.service

import java.time.ZonedDateTime

import airportcapacity.domain.FlightInfo
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

case class FlightInfoConfig(openSkyURL: String) extends AnyVal

class FlightInfoService(logger: LoggingAdapter, config: FlightInfoConfig)(implicit ec: ExecutionContext, mat: Materializer, system: ActorSystem) {
  import airportcapacity.http.AirportCapacityJsonProtocol._
  def getStates(): Future[Seq[FlightInfo]] = {
    println(s"Getting states at ${ZonedDateTime.now()}")
    for {
      response <- Http().singleRequest(HttpRequest(method = HttpMethods.GET, uri = config.openSkyURL))
      entity   <- Unmarshal(response.entity).to[Seq[FlightInfo]]
    } yield entity
  }
}
