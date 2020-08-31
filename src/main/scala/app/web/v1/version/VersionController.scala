package app.web.v1.version

import io.swagger.annotations.{Api, ApiOperation}
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.{CrossOrigin, RequestMapping, RequestMethod, RestController}

import scala.beans.BeanProperty

case class Version(@BeanProperty version: String,
                   @BeanProperty buildHash: String,
                   @BeanProperty buildTime: String)

@CrossOrigin
@RestController
@RequestMapping(Array("/v1"))
@Api(description = "misc endpoints", tags = Array("misc"))
class VersionController(appInfo: AppInfo) {

  private val LOG = LoggerFactory.getLogger(this.getClass)

  @ApiOperation(
    value = "Returns version and build date",
    produces = "application/json",
    code = 200)
  @RequestMapping(value = Array("/version"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  def versionService(): Version = {
    LOG.info(s"Got build info' versionInfo=${appInfo.version}, buildHash=${appInfo.buildHash}, dateTime=${appInfo.buildDate}")
    Version(appInfo.version, appInfo.buildHash, appInfo.buildDate)
  }
}
