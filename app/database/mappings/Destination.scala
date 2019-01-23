package database.mappings

import definedStrings.DatabaseStrings.{BCCValue, CCValue, ToValue}
import slick.jdbc.MySQLProfile.api._


object Destination extends Enumeration {
  type Destination = Value
  val ToAddress = Value(ToValue)
  val CC = Value(CCValue)
  val BCC = Value(BCCValue)

  implicit val destinationMapper = MappedColumnType.base[Destination, String](
    e => e.toString,
    s => Destination.withName(s))
}
