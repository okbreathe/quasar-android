package com.okbreathe.quasar.util

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

val DATE_FORMAT_ISO_EXTENDED = "yyyy-MM-dd'T'HH:mm:ss'Z'"

val DATE_FORMAT_ISO_EXTENDED_NO_ZONE = "yyyy-MMM-dd HH:mm:ss"

// 2017-05-15T03:03:46.657583
val DATE_FORMAT_UTC_EXTENDED = "yyyy-MM-dd'T'HH:mm:ss.SSS"

val DATE_FORMAT_DATE_TIME_LONG = "E, MMM dd yyyy, h:mm:ss aaa"

fun utcToLocal(str: String, fmt: String): String =
  utcToLocal(utcTimestamp(str), fmt)

// TODO Replace this with a more reliable method. Have a feeling
// it may be unreliable around time zone changes
fun utcToLocal(date: Date, fmt: String): String {
  val cal = GregorianCalendar()
  val tz = cal.timeZone
  val sdf = SimpleDateFormat(fmt)
  var ost = tz.rawOffset
  ost += (if (tz.inDaylightTime(Date(date.time + ost))) tz.dstSavings else 0)
  return sdf.format(Date(date.time + ost))
}

fun humanizeDate(date: Date = utcNow()): String =
  utcToLocal(date, DATE_FORMAT_DATE_TIME_LONG)

fun utcDateString(date: Date = utcNow()): String =
  SimpleDateFormat(DATE_FORMAT_UTC_EXTENDED).format(date)

// Returns the NOW in UTC
// https://stackoverflow.com/questions/308683/how-can-i-get-the-current-date-and-time-in-utc-or-gmt-in-java/6697884#6697884
fun utcNow(): Date =
  SimpleDateFormat(DATE_FORMAT_ISO_EXTENDED_NO_ZONE).parse(
    SimpleDateFormat(DATE_FORMAT_ISO_EXTENDED_NO_ZONE).apply {
      setTimeZone(TimeZone.getTimeZone("GMT"))
    }.format(Date())
  )

fun utcDate(str: String): Date = SimpleDateFormat(DATE_FORMAT_UTC_EXTENDED).parse(str)

fun utcTimestamp(date: Date = utcNow()): Timestamp = Timestamp(date.time)

fun utcTimestamp(str: String?): Timestamp = if (str != null) utcTimestamp(utcDate(str)) else utcTimestamp()
