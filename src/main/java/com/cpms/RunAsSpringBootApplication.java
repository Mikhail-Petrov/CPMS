package com.cpms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

//import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Boot launcher that allows to run this application on development machine
 * in embeded mode.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SpringBootApplication
@ComponentScan(basePackages = { 
		"com.cpms.config.web",  
		"com.cpms.config.system",
		"com.cpms.config.security",
		"com.cpms.config.applications"})
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		//SimpleModule.class,
		ErrorMvcAutoConfiguration.class})
public class RunAsSpringBootApplication {
//templates
	 public static void main(String[] args) {
		 SpringApplication.run(RunAsSpringBootApplication.class, args);
	 }
	
}
/*
dataSource.applications.driverClassName=com.mysql.jdbc.Driver
dataSource.applications.url=jdbc:mysql://localhost:3333/competencyjpa_applications?useUnicode=yes&characterEncoding=UTF-8
dataSource.applications.username=mastercjpa
dataSource.applications.password=TestingPassword
hibernate.applications.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
hibernate.applications.hbm2ddl.auto=update
*/