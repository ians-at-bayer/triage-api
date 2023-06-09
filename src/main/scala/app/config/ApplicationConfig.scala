package app.config

import app.config.json.ObjectMapperFactory
import com.bayer.scala.http.SimpleHttp
import com.bayer.scala.jdbc.ScalaJdbcTemplate
import com.bayer.scala.transactions.TransactionalFunction
import com.fasterxml.jackson.databind.ObjectMapper
import javax.sql.DataSource
import org.apache.commons.dbcp2.BasicDataSource
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = Array("app"))
@EnableScheduling
class ApplicationConfig {

  @Bean
  def scalaObjectMapper: ObjectMapper = ObjectMapperFactory.instance

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  //
  // Database
  //
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  @Bean
  def dataSource: BasicDataSource = {
    val ds = new BasicDataSource
    ds.setUrl(applicationProperties.databaseUrl)
    ds.setUsername(applicationProperties.databaseUsername)
    ds.setPassword(applicationProperties.databasePassword)
    ds.setDriverClassName("org.postgresql.Driver")
    ds.setDefaultAutoCommit(false)
    ds
  }

  @Bean
  def txManager(dataSource: DataSource): DataSourceTransactionManager = new DataSourceTransactionManager(dataSource)

  @Bean
  def template(dataSource: DataSource): ScalaJdbcTemplate = new ScalaJdbcTemplate(new JdbcTemplate(dataSource))

  @Bean
  def txFunction(txManager: DataSourceTransactionManager): TransactionalFunction = new TransactionalFunction(txManager)


  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  //
  // HTTP clients
  //
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  @Bean
  def httpClient: CloseableHttpClient = HttpClientBuilder.create
    .setMaxConnPerRoute(32)
    .setMaxConnTotal(32)
    .setUserAgent(getClass.getName)
    .setDefaultRequestConfig(RequestConfig.custom
      .setSocketTimeout(120 * 1000)
      .setConnectTimeout(60 * 1000)
      .setConnectionRequestTimeout(-1).build)
    .build

  @Bean
  def simpleHttp(httpClient: CloseableHttpClient): SimpleHttp = new SimpleHttp(httpClient)

  @Bean
  def applicationProperties = new AppProperties

}
