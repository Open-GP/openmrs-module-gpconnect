package org.openmrs.module.gpconnect;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan
@EnableJpaRepositories("org.openmrs.module.gpconnect.repository")
public class GPConnectConfiguration {
	
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		System.out.println("GPConnect LocalContainerEntityManagerFactoryBean");
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource());
		em.setPackagesToScan("org.openmrs.module.gpconnect");
		
		em.setPersistenceProvider(new HibernatePersistenceProvider());
		em.setJpaProperties(additionalProperties());
		
		return em;
	}
	
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3308/platformserver");
		dataSource.setUsername("root");
		dataSource.setPassword("Admin123");
		return dataSource;
	}
	
	@Bean
	public PlatformTransactionManager transactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
		
		return transactionManager;
	}
	
	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}
	
	Properties additionalProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.hbm2ddl.auto", "none");
		properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
		
		return properties;
	}
}
