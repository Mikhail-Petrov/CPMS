package com.cpms.config.system;

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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.cpms.dao.implementations.jpa.JPADocumentDAO;
import com.cpms.dao.implementations.jpa.JPAKeywordDAO;
import com.cpms.dao.implementations.jpa.JPALanguagesDAO;
import com.cpms.dao.implementations.jpa.JPAMessageDAO;
import com.cpms.dao.implementations.jpa.JPAMotivationDAO;
import com.cpms.dao.implementations.jpa.JPAProfileDAO;
import com.cpms.dao.implementations.jpa.JPARewardsDAO;
import com.cpms.dao.implementations.jpa.JPASkillDraftableDAO;
import com.cpms.dao.implementations.jpa.JPATaskDAO;
import com.cpms.dao.implementations.jpa.JPATermDAO;
import com.cpms.dao.implementations.jpa.JPATermInnovationDAO;
import com.cpms.dao.implementations.jpa.JPATopicDAO;
import com.cpms.dao.implementations.jpa.JPAUserDAO;
import com.cpms.dao.interfaces.IDAO;
import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IInnovationTermDAO;
import com.cpms.dao.interfaces.IUserDAO;
import com.cpms.data.entities.Article;
import com.cpms.data.entities.Keyword;
import com.cpms.data.entities.Language;
import com.cpms.data.entities.Message;
import com.cpms.data.entities.Motivation;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Reward;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.Term;
import com.cpms.data.entities.Topic;
import com.cpms.security.CustomUserDetailsService;
import com.cpms.web.controllers.CommonModelAttributes;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Creates java beans for data access
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@PropertySource("classpath:properties/system.properties")
//@PropertySource("file:system.properties")
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
    
    @Bean(name="mailSender")
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        boolean success = false;
        String host = "smtp.gmail.com", username = "everyths.alr.taken@gmail.com", password = "", auth = "true", starttls = "true";
        int port = 587;
        try {
        	Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			host = (String) initContext.lookup("java:/comp/env/mhost");
			username = (String) initContext.lookup("java:/comp/env/muser");
			password = (String) initContext.lookup("java:/comp/env/mpass");
			auth = (String) initContext.lookup("java:/comp/env/auth");
			port = (Integer) initContext.lookup("java:/comp/env/mport");
			starttls = (String) initContext.lookup("java:/comp/env/starttls");
	        success = true;
		} catch (Exception e) {
			CommonModelAttributes.test(e.getMessage());
		}
        if (!success) {
        	return null;
        }
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
 
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttls);
        props.put("mail.debug", "true");

		CommonModelAttributes.test("success!\n");
        return mailSender;
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
        try {
        	Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			indexBase = (String) initContext.lookup("java:/comp/env/indexBase");
		} catch (Exception e) {
		}
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
    
    private JPATermInnovationDAO termDAO = new JPATermInnovationDAO();
    /**
     * @return implementation of IDAO interface for Term entity
     * @see IDAO
     * @see Task
     */
    @Bean(name = "termDAO")
    public IDAO<Term> getTermDAO() {
    	return new JPATermDAO();
    }
    
    /**
     * @return implementation of IDAO interface for Term entity with innovations
     * @see IDAO
     * @see Task
     */
    @Bean(name = "innovationDAO")
    public IInnovationTermDAO getInnovationDAO() {
    	return termDAO;
    }
    
    /**
     * @return implementation of IDAO interface for Document entity
     * @see IDAO
     * @see Task
     */
    @Bean(name = "docDAO")
    public IDAO<Article> getDocumentDAO() {
    	return new JPADocumentDAO();
    }
    
    /**
     * @return implementation of IDAO interface for Keyword entity
     * @see IDAO
     * @see Task
     */
    @Bean(name = "keywordDAO")
    public IDAO<Keyword> getKeywordDAO() {
    	return new JPAKeywordDAO();
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
    @Bean(name = "rewardsDAO")
    public IDAO<Reward> getRewardsDAO() {
    	return new JPARewardsDAO();
    }
    
    /**
     * @return implementation of IDAO interface for Language entity
     * @see IDAO
     * @see Task
     */
    @Bean(name = "languageDAO")
    public IDAO<Language> getLanguageDAO() {
    	return new JPALanguagesDAO();
    }
    
    /**
     * @return implementation of IDAO interface for Language entity
     * @see IDAO
     * @see Task
     */
    @Bean(name = "topicDAO")
    public IDAO<Topic> getTopicDAO() {
    	return new JPATopicDAO();
    }
    
    /**
     * @return implementation of IDAO interface for Motivation entity
     * @see IDAO
     * @see Task
     */
    @Bean(name = "messageDAO")
    public IDAO<Message> getMessagesDAO() {
    	return new JPAMessageDAO();
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
