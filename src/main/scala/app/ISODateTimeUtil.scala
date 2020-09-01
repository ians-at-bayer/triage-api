package app

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, ZoneId}

/**
  * By convention in this application endpoints that read or return datetime parameters use the ISO_8601 date time
  * format with an explicit UTC timezone (indicated by the trailing Z).  For example '2011-12-03T10:15:30Z'.
  *
  * This class will also successfully parse string with fractional second, for example: '2011-12-03T10:15:30.525Z'
  *
  * https://en.wikipedia.org/wiki/ISO_8601
  * https://en.wikipedia.org/wiki/ISO_8601#Time_zone_designators
  */
object ISODateTimeUtil {

  private val UTC: ZoneId = ZoneId.of("UTC")

  private val DateAndTimeFormat: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT.withZone(UTC)
  private val DateOnlyFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(UTC)

  // - - - - - - - - - - - - - - - - -
  // date + time
  // - - - - - - - - - - - - - - - - -

  def toInstant(string: String): Instant = Instant.from(DateAndTimeFormat.parse(string))

  def toString(instant: Instant): String = DateAndTimeFormat.format(instant)

  // - - - - - - - - - - - - - - - - -
  // date only
  // - - - - - - - - - - - - - - - - -

  def toDateOnly(instant: Instant): String = DateOnlyFormat.format(instant)

  def fromDateOnly(dateOnly: String): Instant = LocalDate.parse(dateOnly).atStartOfDay(UTC).toInstant

}
