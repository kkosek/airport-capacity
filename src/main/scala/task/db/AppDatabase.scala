package task.db

import task.domain.AirportCapacity
import task.repository.AirportCapacities
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

class AppDatabase(override val connector: CassandraConnection) extends Database[AppDatabase](connector) {
  object AirportCapacities extends AirportCapacities with connector.Connector

  def insert(capacity: AirportCapacity): Future[ResultSet] = Batch.logged.add(AirportCapacities.store(capacity)).future()
}

