package app.config

import com.monsanto.mbl.properties.CloudPropertyLoader

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
  def databaseUsername: String = props("database.username")

  def databasePassword: String = props("database.password")

  def databaseUrl: String = props("database.url")

}
