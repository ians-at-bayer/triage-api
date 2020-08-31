package app.config

import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import javax.annotation.Resource
import org.springframework.context.annotation.{ComponentScan, Import}
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.{DefaultServletHandlerConfigurer, EnableWebMvc, ResourceHandlerRegistry, WebMvcConfigurer}
import springfox.documentation.swagger2.configuration.Swagger2DocumentationConfiguration

/**
  * Configuration that sets up WEB-MVC.  This is referenced by WebServletConfiguration and it in turn imports the
  * remaining configuration for the application.
  *
  * So our application initialization is like so:
  *
  * Tomcat -> WebServletConfiguration -> MvcConfig -> ApplicationConfig
  */
@EnableWebMvc
@ComponentScan(basePackages = Array("app"))
@Import(Array(classOf[Swagger2DocumentationConfiguration], classOf[ApplicationConfig]))
class MvcConfig extends WebMvcConfigurer {

  @Resource
  private var scalaObjectMapper: ObjectMapper = _

  override def configureDefaultServletHandling(configurer: DefaultServletHandlerConfigurer): Unit = configurer.enable()

  override def extendMessageConverters(converters: util.List[HttpMessageConverter[_]]): Unit = {
    for (i <- 0 until converters.size()) {
      converters.get(i) match {
        case converter: AbstractJackson2HttpMessageConverter =>
          converter.setObjectMapper(scalaObjectMapper)
        case _ =>
      }
    }
    super.extendMessageConverters(converters)
  }

  override def addResourceHandlers(registry: ResourceHandlerRegistry): Unit = {
    registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/")
    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/")
  }

}
