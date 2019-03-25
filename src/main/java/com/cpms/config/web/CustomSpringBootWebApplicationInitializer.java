package com.cpms.config.web;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.DispatcherServlet;

import com.cpms.config.applications.ApplicationsPersistencyConfig;
import com.cpms.config.security.SecurityConfig;
import com.cpms.config.system.OperationConfig;
import com.cpms.config.system.PersistencyConfig;

/**
 * Implementation of {@link SpringBootServletInitializer}, that works in place
 * of rudimental web.xml.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class})
public class CustomSpringBootWebApplicationInitializer 
										extends SpringBootServletInitializer {
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        SpringApplicationBuilder builder = application.sources(
        		CustomSpringBootWebApplicationInitializer.class,
        		SecurityConfig.class,
        		ApplicationsPersistencyConfig.class,
        		OperationConfig.class,
        		PersistencyConfig.class,
        		WebConfig.class);
        return builder;
    }
	
	@Bean
	public ServletRegistrationBean dispatcherServletRegistration() {
		ServletRegistrationBean registration = 
				new ServletRegistrationBean(dispatcherServlet());
		registration.addUrlMappings("/*");
		return registration;
	}
	
	@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
    public DispatcherServlet dispatcherServlet() {
		DispatcherServlet ds = new DispatcherServlet();
        ds.setThrowExceptionIfNoHandlerFound(true);
        return ds;
    }
	
	@Bean
    @ConditionalOnMissingBean(RequestContextListener.class)
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}
