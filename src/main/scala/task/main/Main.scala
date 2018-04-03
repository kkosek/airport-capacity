package task.main

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.{ActorMaterializer, Materializer}
import com.outworkers.phantom.connectors.ContactPoint
import com.typesafe.config.ConfigFactory
import task.db.{AppDatabase, DbConfig}

import scala.concurrent.ExecutionContext

trait Setup {
  import com.softwaremill.macwire._

  implicit val system: ActorSystem
  implicit val executor: ExecutionContext
  implicit val materializer: Materializer

  lazy val logger = Logging(system, getClass)
  lazy val config = ConfigFactory.load()

  lazy val cassandraConfig = DbConfig(
    hostname                = config.getString("cassandra.hostname"),
    port                    = config.getInt("cassandra.port"),
    replicationStrategy     = config.getString("cassandra.replication-strategy"),
    replicationFactor       = config.getString("cassandra.replication-factor"),
    defaultConsistencyLevel = config.getString("cassandra.default-consistency-level"),
    keyspace                = config.getString("cassandra.keyspace")
  )

  lazy val connector = ContactPoint.apply(cassandraConfig.hostname, cassandraConfig.port).keySpace(cassandraConfig.keyspace)
  lazy val db: AppDatabase = wire[AppDatabase]

}


object Main extends App with Setup {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
}
