package app.config.json

import java.time.Instant

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{DeserializationFeature, MapperFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object ObjectMapperFactory {

  /**
   * Make this singleton object mapper (used to do all JSON serialization application wide) available to static
   * classes (scala objects) and tests that do not have access to spring context.
   */
  val instance: ObjectMapper = doConfig()

  private def doConfig(): ObjectMapper = {
    val instance = new ObjectMapper()
    // Don't fail to serialize if the JSON contains *extra* fields.
    instance.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    // Pretty print the output -- just makes debugging/testing easier.
    instance.configure(SerializationFeature.INDENT_OUTPUT, true)
    // Makes placement of keys deterministic, which makes testing easier.
    instance.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
    instance.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)

    // Configure global custom serializers.
    val module = new SimpleModule()
    module.addSerializer(classOf[Instant], new InstantSerializer)
    module.addDeserializer(classOf[Instant], new InstantDeserializer)
    instance.registerModule(module)

    // Handle serialization to/from scala classes
    instance.registerModule(DefaultScalaModule)
    instance
  }

}
