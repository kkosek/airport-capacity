package airportcapacity.utils

object StringExtensions {
  implicit class StringOps(str: String) {
    def removeDoubleQuotesIfExist(): String =
      str.stripSuffix("\"").stripPrefix("\"")
  }
}
