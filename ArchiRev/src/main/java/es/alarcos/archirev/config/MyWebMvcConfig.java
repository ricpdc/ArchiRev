package es.alarcos.archirev.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.primefaces.webapp.filter.FileUploadFilter;
import org.springframework.boot.web.support.ErrorPageFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MyWebMvcConfig {

	@Bean
	public WebMvcConfigurerAdapter forwardToIndex() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addViewControllers(ViewControllerRegistry registry) {
				registry.addViewController("/").setViewName("forward:/index.xhtml");
				// registry.addViewController("/admin").setViewName("forward:/admin/index.html");
				// registry.addViewController("/user").setViewName("forward:/user/index.html");
			}
		};
	}
	
	@Bean
    public ErrorPageFilter errorPageFilter() {
        return new ErrorPageFilter();
    }

    @Bean
    public FilterRegistrationBean disableSpringBootErrorFilter(ErrorPageFilter filter) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.setEnabled(false);
        return filterRegistrationBean;
    }
    
    
    @Bean
    public FilterRegistrationBean someFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new FileUploadFilter());
        registration.addUrlPatterns("/url/*");
        registration.addInitParameter("thresholdSize", "50000000000");
        registration.addInitParameter("uploadDirectory", "/tmp/fileUpload");
        registration.setName("PrimeFaces FileUpload Filter");
        //registration.setOrder(1);
        return registration;
    } 
    
  

    

}