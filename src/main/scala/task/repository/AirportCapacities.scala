package task.repository

import com.outworkers.phantom.Table
import com.outworkers.phantom.dsl._
import task.domain.AirportCapacity

abstract class AirportCapacities extends Table[AirportCapacities, AirportCapacity] {
  override def tableName: String = "airport_capacity"

  object airportId extends StringColumn with PartitionKey {
    override lazy val name = "airport_id"
  }
  object hour extends StringColumn with ClusteringOrder
  object arrived extends IntColumn
  object departured extends IntColumn
}
