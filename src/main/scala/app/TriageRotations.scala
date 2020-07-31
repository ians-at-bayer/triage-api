package app

import app.config.ApplicationConfig
import org.springframework.context.annotation.AnnotationConfigApplicationContext

object TriageRotations {
  def main(args: Array[String]) : Unit = {
    new AnnotationConfigApplicationContext(classOf[ApplicationConfig])

    while (true) {}
  }
}
