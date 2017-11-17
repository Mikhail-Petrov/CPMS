package com.cpms.config.testing;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import com.cpms.dao.applications.ApplicationsService;
import com.cpms.dao.implementations.jpa.JPAProfileDAO;
import com.cpms.dao.implementations.jpa.JPASkillDraftableDAO;
import com.cpms.dao.implementations.jpa.JPATaskDAO;
import com.cpms.dao.implementations.jpa.JPAUserDAO;
import com.cpms.dao.interfaces.IApplicationsService;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.facade.BasicFacade;
import com.cpms.facade.ICPMSFacade;
import com.cpms.operations.implementations.BasicPossibilityAggregator;
import com.cpms.operations.implementations.BasicProfileComparator;
import com.cpms.operations.implementations.BasicProfileCompetencySearcher;
import com.cpms.operations.implementations.BasicProfileRanger;
import com.cpms.operations.implementations.BasicSubprofiler;
import com.cpms.operations.implementations.BasicTaskComparator;
import com.cpms.operations.interfaces.IPossibilityAggregator;
import com.cpms.operations.interfaces.IProfileComparator;
import com.cpms.operations.interfaces.IProfileCompetencySearcher;
import com.cpms.operations.interfaces.IProfileRanger;
import com.cpms.operations.interfaces.ISubprofiler;
import com.cpms.operations.interfaces.ITaskComparator;
import com.cpms.security.CustomUserDetailsService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Config which creates java beans for testing purposes only
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@PropertySource("classpath:properties/testing.properties")
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		basePackages = {"com.cpms.dao.implementations.jpa.repositories.system",
				"com.cpms.dao.implementations.jpa.repositories.security",
				"com.cpms.dao.implementations.jpa.repositories.applications"},
		entityManagerFactoryRef = "entityManagerFactory",
		transactionManagerRef = "transactionManager")
public class TestingConfig implements TransactionManagementConfigurer {

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
    @Value("${baseUrl}")
    private String baseUrl;
    
    /**
     * @return database access DataSource
     */
    @Bean(name="testingDataSource")
    public DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        return new HikariDataSource(config);
    }
    
    /**
     * @return a bean that will automatically set property representing 
     * current url for web tests
     */
    @Bean
    public MethodInvokingFactoryBean setBaseUrl() {
    	MethodInvokingFactoryBean mifb = new MethodInvokingFactoryBean();
    	mifb.setStaticMethod("com.cpms.tests.web.pagesmock.AbstractPage.setBaseUrl");
    	mifb.setArguments(new Object[]{baseUrl});
    	return mifb;
    }
    
    /**
     * @return embedded data source for faster testing
     */
    @Bean(name="embededTestingDataSource")
    public DataSource getEmbededDataSource() {
    	EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		EmbeddedDatabase db = builder
			.setType(EmbeddedDatabaseType.H2)
			.setName("testing")
			.build();
		return db;
    }

    /**
     * @param dataSource configured database access DataSource
     * @return fully configured LocalContainerEntityManagerFactoryBean
     */
    @Bean(name = "entityManagerFactory")
    @Autowired
    public LocalContainerEntityManagerFactoryBean configureEntityManagerFactory(
    		@Qualifier("embededTestingDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean =
        		new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setPackagesToScan("com.cpms.data", 
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
     * @param transactionManager
     * @return transaction manager for applications service
     */
    @Bean(name = "applicationsTransactionManager")
    @Autowired
    public PlatformTransactionManager applicationsTransactionManager(
    		@Qualifier("transactionManager") 
    			JpaTransactionManager transactionManager) {
    	return transactionManager;
    }

    /**
     * @param transactionManager
     * @return main transaction manager
     */
    @Bean(name = "transactionManager")
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new JpaTransactionManager();
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
     * @return implementation of IDAO interface for Task entity
     * @see IDAO
     * @see Task
     */
    @Bean(name = "taskDAO")
    public IDAO<Task> getTaskDAO() {
    	return new JPATaskDAO();
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
     * @return DAO to hold drafts of Skill entity
     * @see IDraftableSkillDaoExtension
     */
    @Bean(name = "draftableSkillDAO")
    public IDraftableSkillDaoExtension getDraftableSkillDAO() {
    	return skillDao;
    }
    
    /**
     * @return fully configured custom DAO for user entities to be used
     * by Spring Security
     */
    @Bean(name = "userDetailsService")
    public UserDetailsService getUserDetailsService() {
    	return new CustomUserDetailsService();
    }
    
    /**
     * @return implementation of ICPMSFacade to be used in testing
     * @see ICPMSFacade
     */
    @Bean(name = "facade")
    public ICPMSFacade getFacade() {
    	BasicFacade facade = new BasicFacade();
    	return facade;
    }
    
    /**
     * @return congfigured and ready for use implementation of IApplicationsService
     * @see IApplicationsService
     */
    @Bean(name = "applicationsService")
    public IApplicationsService getApplicationsService() {
    	ApplicationsService service = new ApplicationsService();
    	return service;
    }
    
    /**
	 * @return implementation of IPossibilityAggregator
	 * @see IPossibilityAggregator
	 */
    @Bean(name = "possibilityAggregator")
	public IPossibilityAggregator getPossibilityAggregator() {
		BasicPossibilityAggregator target = new BasicPossibilityAggregator();
		target.setComparator(getTaskComparator());
		return target;
	}
	
    /**
	 * @return implementation of IProfileComparator
	 * @see IProfileComparator
	 */
	@Bean(name = "profileComparator")
	public IProfileComparator getProfileComparator() {
		return new BasicProfileComparator();
	}
	
	/**
	 * @return implementation of IProfileCompetencySearcher
	 * @see IProfileCompetencySearcher
	 */
	@Bean(name = "profileCompetencySearcher")
	public IProfileCompetencySearcher getProfileCompetencySearcher() {
		BasicProfileCompetencySearcher target =
				new BasicProfileCompetencySearcher();
		target.setComparator(getProfileComparator());
		return target;
	}
	
	/**
	 * @return implementation of IProfileRanger
	 * @see IProfileRanger
	 */
	@Bean(name = "profileRanger")
	public IProfileRanger getProfileRanger() {
		BasicProfileRanger target = new BasicProfileRanger();
		target.setComparator(getProfileComparator());
		return target;
	}
	
	/**
	 * @return implementation of ISubprofiles
	 * @see ISubprofiler
	 */
	@Bean(name = "subprofiler")
	public ISubprofiler getSubprofiler() {
		return new BasicSubprofiler();
	}
	
	/**
	 * @return implementation of ITaskComparator
	 * @see ITaskComparator
	 */
	@Bean(name = "taskComparator")
	public ITaskComparator getTaskComparator() {
		return new BasicTaskComparator();
	}
}
