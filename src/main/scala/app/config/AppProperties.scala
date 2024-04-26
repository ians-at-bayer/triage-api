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
  def databaseUsername: String = sys.env.getOrElse("DB_USER", "triagerotationsuser")

  def databasePassword: String = sys.env.getOrElse("DB_PASS", "triagerotationsuser")

  def databaseUrl: String = sys.env.getOrElse("DB_URL", "jdbc:postgresql://postgres:5432/triage_rotations?currentSchema=triage_rotations_local&ssl=false")

  //
  // Base URL
  //
  def baseUrl: String = sys.env.getOrElse("BASE_URL", "http://localhost/support-triage-manager")

}
