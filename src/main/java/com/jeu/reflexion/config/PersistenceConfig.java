package com.jeu.reflexion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.jeu.reflexion.repository")
@PropertySource("classpath:application.properties")
public class PersistenceConfig {

    private final Environment env;

    public PersistenceConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("db.driver", "org.h2.Driver"));
        dataSource.setUrl(env.getProperty("db.url", "jdbc:h2:file:./data/jeudb"));
        dataSource.setUsername(env.getProperty("db.username", "sa"));
        dataSource.setPassword(env.getProperty("db.password", ""));
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.jeu.reflexion.model");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProps = new Properties();
        jpaProps.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect"));
        jpaProps.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto", "update"));
        jpaProps.setProperty("hibernate.show_sql", env.getProperty("hibernate.show_sql", "false"));
        emf.setJpaProperties(jpaProps);
        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean emf) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(emf.getObject());
        return txManager;
    }

    @Bean
    @DependsOn("entityManagerFactory")
    public DatabaseSchemaFixer databaseSchemaFixer(DataSource dataSource) {
        return new DatabaseSchemaFixer(dataSource);
    }
}
