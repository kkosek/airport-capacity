package airportcapacity.service

import java.time.ZonedDateTime

import airportcapacity.db.AppDatabase
import airportcapacity.domain.{AirportCapacity, FlightInfo, Position}
import akka.event.LoggingAdapter
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Supervision.{Decider, Restart, Resume}
import akka.stream.ThrottleMode
import akka.stream.contrib.Pulse
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.outworkers.phantom.ResultSet
import enumeratum._

import scala.concurrent.duration._
import scala.collection.immutable.IndexedSeq
import scala.concurrent.Future

sealed trait FlightStatus extends EnumEntry
object FlightStatus extends Enum[FlightStatus] {
  case object Departured extends FlightStatus
  case object Arrived extends FlightStatus
  case object Other extends FlightStatus

  val values: IndexedSeq[FlightStatus] = findValues
}

case class FlightStatusWithPosition(position: Option[Position],
                                    status: FlightStatus)

case class AggregatorConfig(collectDataDuration: FiniteDuration, writeToDbDuration: FiniteDuration)

class AggregatorService(airportService: AirportService,
                        flightInfoService: FlightInfoService,
                        db: AppDatabase,
                        config: AggregatorConfig,
                        logger: LoggingAdapter) {
  private def resumingDecider(implicit log: LoggingAdapter): Decider = e => {
    log.error(e, "Resuming after error")
    Resume
  }

  private def restartingDecider(implicit log: LoggingAdapter): Decider = e => {
    log.error(e, "Restarting after error")
    Restart
  }

  def restartOnError[In, Out, Mat](flow: Flow[In, Out, Mat])(
      implicit log: LoggingAdapter): Flow[In, Out, Mat] =
    flow.withAttributes(supervisionStrategy(restartingDecider))

  val aggregateData = Flow[Seq[FlightInfo]]
    .conflate((seqA, seqB) => seqA ++ seqB)
    .via(new Pulse(config.writeToDbDuration, false))

  val getStates = Flow[String].mapAsync(1)(flightInfoService.getStates)
  val source = Source
    .repeat("")
    .throttle(elements = 1,
              per = config.collectDataDuration,
              maximumBurst = 1,
              mode = ThrottleMode.Shaping)
    .via(restartOnError(getStates)(logger))
    .via(restartOnError(aggregateData)(logger))

  val cassandra = Flow[Seq[FlightInfo]].map(aggregate).to(Sink.foreach(println))

  private def aggregate(
      flightInfos: Seq[FlightInfo]): Future[Seq[ResultSet]] = {
    val airports = airportService.getAirports()
    val flights = flightInfos
      .groupBy(_.id)
      .map {
        case (_, infos) => flightStatusesWithPosition(infos)
      }
      .filter(s => s.status != FlightStatus.Other)
    val xd = airports
      .map { airport =>
        val flightsNearAirport =
          flights.filter(_.position.fold(false)(_.isNear(airport.position)))
        val arrived = flightsNearAirport.count(_.status == FlightStatus.Arrived)
        val departured =
          flightsNearAirport.count(_.status == FlightStatus.Departured)
        AirportCapacity(airport.id.id,
                        ZonedDateTime.now().toString,
                        arrived,
                        departured)
      }
      .map(capacity => db.insert(capacity))
    Future.sequence(xd)
  }

  private def flightStatusesWithPosition(
      flightInfos: Seq[FlightInfo]): FlightStatusWithPosition =
    flightInfos match {
      case infos if infos.forall(_.onGround == infos.head.onGround) =>
        FlightStatusWithPosition(None, FlightStatus.Other)
      case i :: infos if i.onGround =>
        FlightStatusWithPosition(infos.headOption.flatMap(_.position),
                                 FlightStatus.Departured)
      case infos =>
        FlightStatusWithPosition(
          infos.find(_.onGround == true).flatMap(_.position),
          FlightStatus.Arrived)
      case _ => FlightStatusWithPosition(None, FlightStatus.Other)
    }

}
