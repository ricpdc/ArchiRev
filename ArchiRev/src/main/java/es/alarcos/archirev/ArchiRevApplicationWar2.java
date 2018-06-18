
package es.alarcos.archirev;

import java.util.HashMap;
import java.util.Map;

import javax.faces.webapp.FacesServlet;
import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.joinfaces.integration.ViewScope;
import org.primefaces.webapp.filter.FileUploadFilter;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.ServletContextAware;

import com.sun.faces.config.ConfigureListener;

@SpringBootApplication
public class ArchiRevApplicationWar2 extends SpringBootServletInitializer implements ServletContextAware {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ArchiRevApplicationWar2.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ArchiRevApplicationWar2.class);
	}

	@Bean
	public ServletContextInitializer servletContextCustomizer() {
		return new ServletContextInitializer() {
			@Override
			public void onStartup(ServletContext servletContext) throws ServletException {
				servletContext.setInitParameter("primefaces.CLIENT_SIDE_VALIDATION", "true");
				servletContext.setInitParameter("javax.faces.FACELETS_REFRESH_PERIOD", "0");
				servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Development");
				servletContext.setInitParameter("javax.faces.FACELETS_SKIP_COMMENTS", Boolean.TRUE.toString());
				servletContext.setInitParameter("primefaces.FONT_AWESOME", Boolean.TRUE.toString());
				servletContext.setInitParameter("primefaces.UPLOADER", "commons");
				servletContext.setInitParameter("facelets.DEVELOPMENT", "true");
				servletContext.setInitParameter("primefaces.THEME", "bootstrap");
			}
		};
	}

	@Bean
	public ServletRegistrationBean facesServlet() {
		FacesServlet servlet = new FacesServlet();
		ServletRegistrationBean registration = new ServletRegistrationBean(servlet, "*.xhtml");
		registration.setName("Faces Servlet");
		registration.setLoadOnStartup(1);
		registration.setMultipartConfig(new MultipartConfigElement((String) null));
		return registration;
	}

	@Bean
	public ServletListenerRegistrationBean<ConfigureListener> jsfConfigureListener() {
		return new ServletListenerRegistrationBean<ConfigureListener>(new ConfigureListener());
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", Boolean.TRUE.toString());
	}

	@Bean
	public FilterRegistrationBean facesUploadFilterRegistration() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean(new FileUploadFilter(), facesServlet());
		registrationBean.setName("PrimeFaces FileUpload Filter");
		registrationBean.addUrlPatterns("Faces Servlet");
		registrationBean.setDispatcherTypes(DispatcherType.FORWARD, DispatcherType.REQUEST);
		return registrationBean;
	}

	@Bean
	public static CustomScopeConfigurer customScopeConfigurer() {
		CustomScopeConfigurer configurer = new CustomScopeConfigurer();
		Map<String, Object> scopes = new HashMap<String, Object>();
		scopes.put("view", new ViewScope());
		configurer.setScopes(scopes);
		return configurer;
	}
}
