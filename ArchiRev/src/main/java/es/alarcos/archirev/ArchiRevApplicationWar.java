
package es.alarcos.archirev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class ArchiRevApplicationWar extends SpringBootServletInitializer {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ArchiRevApplicationWar.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ArchiRevApplicationWar.class);
	}
}
