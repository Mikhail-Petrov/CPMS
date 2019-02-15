package com.cpms.config.system;

import java.util.Properties;

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

import com.cpms.dao.implementations.jpa.JPALanguagesDAO;
import com.cpms.dao.implementations.jpa.JPAMotivationDAO;
import com.cpms.dao.implementations.jpa.JPAProfileDAO;
import com.cpms.dao.implementations.jpa.JPASkillDraftableDAO;
import com.cpms.dao.implementations.jpa.JPATaskDAO;
import com.cpms.dao.implementations.jpa.JPAUserDAO;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Language;
import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.security.CustomUserDetailsService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Creates java beans for data access
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@PropertySource("classpath:properties/system.properties")
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		basePackages = {"com.cpms.dao.implementations.jpa.repositories.system",
				"com.cpms.dao.implementations.jpa.repositories.security"},
		entityManagerFactoryRef = "entityManagerFactory",
		transactionManagerRef = "transactionManager")
public class PersistencyConfig {
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfig() {
		return new PropertySourcesPlaceholderConfigurer();
	}

    @Value("${dataSource.driverClassName}")
    private String driver;
    @Value("${dataSource.url}")
    private String url;
    @Value("${dataSource.username}")
    private String username;
    @Value("${dataSource.password}")
    private String password;
    @Value("${hibernate.dialect}")
    private String dialect;
    @Value("${hibernate.hbm2ddl.auto}")
    private String hbm2ddlAuto;
    @Value("${hibernate.search.default.directory_provider}")
    private String directoryProvider;
    @Value("${hibernate.search.default.indexBase}")
    private String indexBase;
    @Value("${hibernate.search.default.refresh}")
    private String refreshRate;

    /**
     * @return database access DataSource
     */
    @Bean(name="dataSource")
    public DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        return new HikariDataSource(config);
    }

    /**
     * @param dataSource configured database access DataSource
     * @return fully configured LocalContainerEntityManagerFactoryBean
     */
    @Bean(name = "entityManagerFactory")
    @Autowired
    public LocalContainerEntityManagerFactoryBean configureEntityManagerFactory(
    		@Qualifier("dataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = 
        		new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setPackagesToScan("com.cpms.data.entities", 
        		"com.cpms.security.entities");
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put(org.hibernate.cfg.Environment.DIALECT, dialect);
        jpaProperties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, hbm2ddlAuto);
        jpaProperties.put("hibernate.search.default.directory_provider",
        		directoryProvider);
        jpaProperties.put("hibernate.search.default.indexBase", indexBase);
        jpaProperties.put("hibernate.search.default.refresh", refreshRate);
        entityManagerFactoryBean.setJpaProperties(jpaProperties);

        return entityManagerFactoryBean;
    }

    /**
     * @param entityManager fully configured entity manager factory
     * @return transaction manager
     */
    @Bean(name = "transactionManager")
    @Autowired
    public PlatformTransactionManager transactionManager(
    		@Qualifier("entityManagerFactory") 
    			LocalContainerEntityManagerFactoryBean entityManager) {
        return new JpaTransactionManager(entityManager.getObject());
    }
    
    /**
     * @return implementation of IDAO interface for Profile entity
     * @see IDAO
     * @see Profile
     */
    @Bean(name = "profileDAO")
    public IDAO<Profile> getProfileDAO() {
    	return new JPAProfileDAO();
    }
    
    private JPASkillDraftableDAO skillDao = new JPASkillDraftableDAO();
    
    /**
    * @return implementation of IDAO interface for Skill entity
    * @see IDAO
    * @see Skill
    */
    @Bean(name = "skillDAO")
    public IDAO<Skill> getSkillDAO() {
    	return skillDao;
    }
    
    /**
     * @return DAO to hold drafts of Skill entity
     * @see IDraftableSkillDaoExtension
     */
    @Bean(name = "draftableSkillDAO")
    public IDraftableSkillDaoExtension getDraftableSkillDAO() {
    	return skillDao;
    }
    
    /**
     * @return implementation of IDAO interface for Task entity
     * @see IDAO
     * @see Task
     */
    @Bean(name = "taskDAO")
    public IDAO<Task> getTaskDAO() {
    	return new JPATaskDAO();
    }
    
    /**
     * @return implementation of IDAO interface for Motivation entity
     * @see IDAO
     * @see Task
     */
    @Bean(name = "motivationDAO")
    public IDAO<Motivation> getMotivationDAO() {
    	return new JPAMotivationDAO();
    }
    
    /**
     * @return implementation of IDAO interface for Motivation entity
     * @see IDAO
     * @see Task
     */
    @Bean(name = "languageDAO")
    public IDAO<Language> getLanguageDAO() {
    	return new JPALanguagesDAO();
    }
    
    /**
     * @return DAO to work with stored entities of users
     * @see IUserDAO
     */
    @Bean(name = "userDAO")
	public IUserDAO getUserDAO() {
		return new JPAUserDAO();
	}
	
    /**
     * @return fully configured custom DAO for user entities to be used
     * by Spring Security
     */
	@Bean(name = "userDetailsService")
    public CustomUserDetailsService customUserDetailsService() {
		CustomUserDetailsService service = new CustomUserDetailsService();
		return service;
	}
}
