package app.config

import com.monsanto.mbl.properties.CloudPropertyLoader
import org.springframework.stereotype.Component

@Component
class AppProperties {

  //
  // Get initial config from VCAP_SERVICES, if defined, else from local json file.
  //
  private val cf = new CloudPropertyLoader(Some("./local-bindings.json"), None)
  private val props = cf.props

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  //
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  //
  // Database
  //
  def databaseUsername: String = props("config.dbUsername")

  def databasePassword: String = props("config.dbPassword")

  def databaseUrl: String = props("config.dbUrl")

  //
  // Base URL
  //
  def baseUrl: String = props("config.baseUrl")

}
