package app.config.json

import java.time.Instant

import app.ISODateTimeUtil
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer}

/**
 * Deserializes a json string value like '2020-01-10T15:17:24Z' as an Instant.
 *
 * If the string representation DOES NOT contain an explicit time zone indicator we add one, which means we are
 * assuming the string to represent UTC times (check with the source endpoint to be sure).
 *
 * If an API returns a ISO_8601 formatted string with an implicit zone where the zone is not UTC then this deserializer
 * cannot be used-- APIs should always use an explicit time zone to avoid confusion and/or possible error!
 */
class InstantDeserializer extends JsonDeserializer[Instant] {

  override def deserialize(p: JsonParser, context: DeserializationContext): Instant = {
    val oc = p.getCodec
    val node: TextNode = oc.readTree(p)
    val nodeString = node.asText().toUpperCase
    val instantString = if (nodeString.endsWith("Z")) nodeString else (nodeString + "Z")
    ISODateTimeUtil.toInstant(instantString)
  }

}
