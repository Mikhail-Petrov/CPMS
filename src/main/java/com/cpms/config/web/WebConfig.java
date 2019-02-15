package com.cpms.config.web;

import java.nio.charset.Charset;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import com.cpms.facade.BasicFacade;
import com.cpms.facade.ICPMSFacade;
import com.cpms.web.UserSessionData;

/**
 * Configuration for the web application part of the project
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.cpms.web"})
public class WebConfig extends WebMvcConfigurerAdapter {
	
	/**
	 * Sets favicon link.
	 * 
	 * @author Gordeev Boris
	 * @since 1.0
	 *//*
	@Controller
    static class FaviconController {
        @RequestMapping("favicon.ico")
        String favicon() {
            return "forward:/resources/images/favicon.ico";
        }
    }*/

	/**
	 * @return template engine configured with template resolver and
	 * message source.
	 */
	@Bean(name = "templateEngine")
	public SpringTemplateEngine getTemplateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setTemplateResolver(getTemplateResolver());
		engine.setMessageSource(messageSource());
		return engine;
	}
	
	/**
	 * @return fully configured template resolver
	 */
	@Bean(name = "templateResolver")
	public SpringResourceTemplateResolver getTemplateResolver() {
		SpringResourceTemplateResolver resolver =
				new SpringResourceTemplateResolver();
		resolver.setPrefix("pages/templates/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode("HTML5");
		resolver.setCharacterEncoding("UTF-8");
		return resolver;
	}
	
	/**
	 * @return fully configured view resolver
	 */
	@Bean(name = "viewResolver")
	public ThymeleafViewResolver getViewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(getTemplateEngine());
		resolver.setOrder(1);
		resolver.setCharacterEncoding("UTF-8");
		resolver.setContentType("text/html; charset=UTF-8");
		return resolver;
	}
	
	/**
	 * @return message converter to help enforce UTF-8 application-wise
	 */
	@Bean
	public StringHttpMessageConverter stringHttpMessageConverter() {
	    return new StringHttpMessageConverter(Charset.forName("UTF-8"));
	}
	
	@Bean(name = "userSessionData")
	@Scope(value = WebApplicationContext.SCOPE_SESSION,
		proxyMode = ScopedProxyMode.TARGET_CLASS)
	public UserSessionData getUserSessionData() {
		return new UserSessionData();
	}
	
	/**
	 * @return configured implementation of {@link ICPMSFacade}
	 * @see ICPMSFacade
	 */
	@Bean(name = "facade")
	@Scope(value = WebApplicationContext.SCOPE_APPLICATION)
	public ICPMSFacade getFacade() {
		return new BasicFacade();
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**")
			.addResourceLocations("/resources/");
	}

	@Override
	public void configureDefaultServletHandling(
			DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}
	
	/**
	 * @return layout interceptor to be registered manually within application
	 * @see LayoutInterceptor
	 */
	@Bean
    public Object getLayoutInterceptor() {
		return new LayoutInterceptor("layout/layout", "_VIEW_TEMPLATE");
	}
	
	/**
	 * @return handler mapping configured with custom interceptors
	 * to be registered by Spring
	 */
	@Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
       RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
       LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
       interceptor.setParamName("language");
       mapping.setOrder(0);
       mapping.setInterceptors(new Object[] {
    		   getLayoutInterceptor(),
    		   interceptor});
       return mapping;
    }
	
	/**
	 * @return source of messages to be used with localization
	 */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = 
        		new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("/localization/msg");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
    
    /**
     * @return locale resolver to help organize locale managing
     */
    @Bean
    public LocaleResolver localeResolver(){
    	CookieLocaleResolver resolver = new CookieLocaleResolver();
    	resolver.setDefaultLocale(new Locale("en"));
    	return resolver;
    }
    
}
