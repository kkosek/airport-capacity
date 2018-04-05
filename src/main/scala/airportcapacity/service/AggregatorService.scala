package airportcapacity.service

import airportcapacity.db.AppDatabase
import airportcapacity.domain.{AirportCapacity, FlightInfo, Position}
import akka.event.LoggingAdapter
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Supervision.{Decider, Restart, Resume}
import akka.stream.ThrottleMode
import akka.stream.contrib.Pulse
import akka.stream.scaladsl.{Flow, Sink, Source}
import enumeratum._

import scala.concurrent.duration._
import scala.collection.immutable.IndexedSeq

sealed trait FlightStatus extends EnumEntry
object FlightStatus extends Enum[FlightStatus] {
  case object Departured extends FlightStatus
  case object Arrived extends FlightStatus
  case object Other extends FlightStatus

  val values: IndexedSeq[FlightStatus] = findValues
}

case class FlightStatusWithPosition(position: Option[Position], status: FlightStatus)


class AggregatorService(airportService: AirportService, flightInfoService: FlightInfoService, db: AppDatabase, logger: LoggingAdapter) {
  private def resumingDecider(implicit log: LoggingAdapter): Decider = e => {
    log.error(e, "Resuming after error")
    Resume
  }

  private def restartingDecider(implicit log: LoggingAdapter): Decider = e => {
    log.error(e, "Restarting after error")
    Restart
  }

  def restartOnError[In, Out, Mat](flow: Flow[In, Out, Mat])(implicit log: LoggingAdapter): Flow[In, Out, Mat] =
    flow.withAttributes(supervisionStrategy(restartingDecider))

  val fl = Flow[Seq[FlightInfo]].conflate((seqA, seqB) => seqA ++ seqB).via(new Pulse(60.seconds, false))

  val getStates = Flow[String].mapAsync(1)(flightInfoService.getStates)
  val src = Source.repeat("")
    .throttle(elements = 1, per = 5.seconds, maximumBurst = 1, mode = ThrottleMode.Shaping)
    .via(restartOnError(getStates)(logger))
    .via(fl)

  val sink = Flow[Seq[FlightInfo]].map(_.size).to(Sink.foreach(println))


  private def aggregate(flightInfos: Seq[FlightInfo]): Seq[AirportCapacity] = {
    val airports = airportService.getAirports()
    val xd = flightInfos.groupBy(_.id).map {
      case (_, infos) => flightStatusesWithPosition(infos)
    }.filter(s => s.status != FlightStatus.Other || s.position.fold(false)(p => airports.exists(_.position.isNear(p))))
  }

  private def flightStatusesWithPosition(flightInfos: Seq[FlightInfo]): FlightStatusWithPosition = flightInfos match {
    case infos if infos.forall(_.onGround == infos.head.onGround) => FlightStatusWithPosition(None, FlightStatus.Other)
    case i :: infos if i.onGround => FlightStatusWithPosition(infos.headOption.flatMap(_.position), FlightStatus.Departured)
    case infos => FlightStatusWithPosition(infos.find(_.onGround == true).flatMap(_.position), FlightStatus.Arrived)
    case _ => FlightStatusWithPosition(None, FlightStatus.Other)
  }


}
