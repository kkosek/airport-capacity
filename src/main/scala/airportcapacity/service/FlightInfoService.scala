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

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

case class FlightInfoConfig(openSkyURL: String) extends AnyVal

case class OpenSkyData(time: Int, states: List[FlightInfo])

class FlightInfoService(logger: LoggingAdapter, config: FlightInfoConfig)(implicit ec: ExecutionContext, mat: Materializer, system: ActorSystem) {
  import airportcapacity.http.AirportCapacityJsonFormats._
  def getStates(str: String): Future[Seq[FlightInfo]] = {
    println(s"Getting states at ${ZonedDateTime.now()}")
    Http().singleRequest(HttpRequest(method = HttpMethods.GET, uri = config.openSkyURL)).flatMap { response =>
      response.entity.toStrict(5.seconds).flatMap { entity =>
        Unmarshal(entity).to[OpenSkyData].map(_.states)
      }
    }
  }
}
