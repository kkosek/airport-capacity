package airportcapacity.db

case class DbConfig(hostname: String, port: Int, replicationStrategy: String, replicationFactor: String, defaultConsistencyLevel: String, keyspace: String)
