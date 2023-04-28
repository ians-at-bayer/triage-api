package app.config

import org.springframework.stereotype.Component

@Component
class AppProperties {

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  //
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  //
  // Database
  //
  def databaseUsername: String = System.getProperty("tr.db.user")

  def databasePassword: String = System.getProperty("tr.db.pass")

  def databaseUrl: String = System.getProperty("tr.db.url")

  //
  // Base URL
  //
  def baseUrl: String = System.getProperty("tr.base.url")

}
