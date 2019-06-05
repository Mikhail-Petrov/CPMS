package com.cpms.config.applications;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.cpms.dao.applications.ApplicationsService;
import com.cpms.dao.interfaces.IApplicationsService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Persistency config for applications, such as competency and evidence.
 * Currently works through second data connection.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@PropertySource("classpath:properties/applications.properties")
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		basePackages = {"com.cpms.dao.implementations.jpa.repositories.applications"},
		entityManagerFactoryRef = "applicationsEntityManagerFactory",
		transactionManagerRef = "applicationsTransactionManager")
public class ApplicationsPersistencyConfig {

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfig() {
		return new PropertySourcesPlaceholderConfigurer();
	}

    @Value("${dataSource.applications.driverClassName}")
    private String driver;
    @Value("${dataSource.applications.url}")
    private String url;
    @Value("${dataSource.applications.username}")
    private String username;
    @Value("${dataSource.applications.password}")
    private String password;
    @Value("${hibernate.applications.dialect}")
    private String dialect;
    @Value("${hibernate.applications.hbm2ddl.auto}")
    private String hbm2ddlAuto;

    /**
     * @return data source for database
     */
    @Bean(name="applicationsDataSource")
    public DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        boolean success = false;
        try {
        	Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
	        config.setJdbcUrl((String) initContext.lookup("java:/comp/env/url"));
	        config.setUsername((String) initContext.lookup("java:/comp/env/username"));
	        config.setPassword((String) initContext.lookup("java:/comp/env/password"));
	        success = true;
		} catch (NamingException e) {
			//CommonModelAttributes.test(e.getMessage());
			//e.printStackTrace();
		}
        if (!success) {
	        config.setJdbcUrl(url);
	        config.setUsername(username);
	        config.setPassword(password);
        }

        return new HikariDataSource(config);
    }

    @Bean(name = "filterMultipartResolver")
    public CommonsMultipartResolver filterMultipartResolver() {
       CommonsMultipartResolver filterMultipartResolver = new CommonsMultipartResolver();
       filterMultipartResolver.setDefaultEncoding("utf-8");
       filterMultipartResolver.setMaxUploadSize(10*1024*1024);
       return filterMultipartResolver;
 }
    
    /**
     * @param dataSource data source for database
     * @return fully configured LocalContainerEntityManagerFactoryBean
     */
    @Bean(name = "applicationsEntityManagerFactory")
    @Autowired
    public LocalContainerEntityManagerFactoryBean configureEntityManagerFactory(
    		@Qualifier("applicationsDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = 
        		new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setPackagesToScan("com.cpms.data.applications");
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put(org.hibernate.cfg.Environment.DIALECT, dialect);
        jpaProperties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, hbm2ddlAuto);
        entityManagerFactoryBean.setJpaProperties(jpaProperties);

        return entityManagerFactoryBean;
    }

    /**
     * @param entityManager fully configured entity manager factory
     * @return JpaTranstactionManager
     */
    @Bean(name = "applicationsTransactionManager")
    @Autowired
    public PlatformTransactionManager transactionManager(
    		@Qualifier("applicationsEntityManagerFactory") 
    			LocalContainerEntityManagerFactoryBean entityManager) {
        return new JpaTransactionManager(entityManager.getObject());
    }
    
    /**
     * @return congfigured and ready for use implementation of IApplicationsService
     * @see IApplicationsService
     */
    @Bean(name = "applicationsService")
    public IApplicationsService applicationsService() {
    	return new ApplicationsService();
    }
	
}
