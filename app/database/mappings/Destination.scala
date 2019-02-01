package database.mappings

import definedStrings.DatabaseStrings.{BCCValue, CCValue, ToValue}
import slick.jdbc.JdbcType
import slick.jdbc.MySQLProfile.api._

object Destination extends Enumeration {
  type Destination = Value
  val ToAddress: Value = Value(ToValue)
  val CC: Value = Value(CCValue)
  val BCC: Value = Value(BCCValue)

  implicit val destinationMapper: JdbcType[Destination] = MappedColumnType.base[Destination, String](
    destination => destination.toString,
    str => Destination.withName(str))
}
