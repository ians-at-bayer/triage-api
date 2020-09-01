package app.web.v1.misc

import java.util.jar.{Attributes, Manifest}

import org.springframework.stereotype.Component

import scala.beans.BeanProperty

@Component
class AppInfo {

  private val sourceResource = "META-INF/MANIFEST.MF"
  private val unknown: String = "Unknown"

  private val specificationTitleName = "Specification-Title"
  private val specificationTitleValue = "support-triage-rotations-manager"

  @BeanProperty
  lazy val version: String = getManifestAttribute("Implementation-Version")

  @BeanProperty
  lazy val buildHash: String = getManifestAttribute("Vcs-Release-Hash")

  @BeanProperty
  lazy val buildDate: String = getManifestAttribute("Build-Date")

  def getManifestAttribute(attributeName: String): String = {
    try {
      val resources = Thread.currentThread().getContextClassLoader.getResources(sourceResource)
      while (resources.hasMoreElements) {
        val manifestInputStream = resources.nextElement().openStream()
        val manifest: Manifest = new Manifest(manifestInputStream)
        val attributes: Attributes = manifest.getMainAttributes
        val title: String = attributes.getValue(specificationTitleName)

        if (specificationTitleValue == title) {
          val value: String = attributes.getValue(attributeName)
          return if (value == null) unknown else value
        }
      }
      unknown
    } catch {
      case _: Throwable =>
        unknown
    }
  }
}