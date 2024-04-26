package app.config

import javax.servlet.ServletContext
import org.springframework.web.WebApplicationInitializer
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.servlet.DispatcherServlet

/**
  * Does the configuration that would traditionally be in web.xml
  */
class WebServletConfiguration extends WebApplicationInitializer {

  override def onStartup(ctx: ServletContext): Unit = {

    //
    // spring context initialization
    //
    val webCtx: AnnotationConfigWebApplicationContext = new AnnotationConfigWebApplicationContext
    webCtx.register(classOf[MvcConfig])
    webCtx.setServletContext(ctx)

    //
    // dispatcher servlet
    //
    val servlet = ctx.addServlet("dispatcher", new DispatcherServlet(webCtx))
    servlet.addMapping("/support-triage-manager-api/*")
    servlet.addMapping("/triton-strm/*")
    servlet.addMapping("/*")
    servlet.setLoadOnStartup(1)

  }

}
