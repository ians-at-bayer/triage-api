package app.config.json

import java.time.Instant

import app.config.ISODateTimeUtil
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class InstantSerializer extends JsonSerializer[Instant] {

  override def serialize(value: Instant, gen: JsonGenerator, serializers: SerializerProvider): Unit =
    gen.writeString(ISODateTimeUtil.toString(value))
}
