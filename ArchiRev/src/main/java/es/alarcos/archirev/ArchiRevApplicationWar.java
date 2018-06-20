
package es.alarcos.archirev;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ArchiRevApplicationWar extends SpringBootServletInitializer {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ArchiRevApplicationWar.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ArchiRevApplicationWar.class);
	}
	
	@Bean
	public ServletContextInitializer servletContextCustomizer() {
		return new ServletContextInitializer() {
			@Override
			public void onStartup(ServletContext servletContext) throws ServletException {
				servletContext.setInitParameter("primefaces.FONT_AWESOME", Boolean.TRUE.toString());
				servletContext.setInitParameter("primefaces.THEME", "bootstrap");
			}
		};
	}
}
