package com.ltimindtree.iMigrate.config;

/*import java.util.HashMap;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.RequiredArgsConstructor;*/

/*@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor*/
public class DataSourceConfig {

  /*  @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariConfig defaultDataSourceProperties() {
        return new HikariConfig();
    }

    @Bean(name = "defaultDatasource")
    @Primary
    public HikariDataSource defaultDataSource() {
        return new HikariDataSource(defaultDataSourceProperties());
    }

    /*@Bean
    @ConfigurationProperties("spring.mssql-datasource.hikari")
    public HikariConfig mssqlDataSourceProperties() {
        return new HikariConfig();
    }


    @Bean(name = "mssql-datasource")
    public HikariDataSource mssqlDataSource() {
    	return new HikariDataSource(mssqlDataSourceProperties());
    }*/

  /*
    @Bean
    @Primary
     public PlatformTransactionManager transactionManager() {
 	   JpaTransactionManager transactionManager = new JpaTransactionManager();
 	   return transactionManager;
     }
    */ 

    /*@Bean
    @Primary
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), new HashMap<>(), null);
    }

    @Primary
    @PersistenceContext(unitName = "routerPU")
    @Bean(name = "routerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean routerEntityManagerFactory(EntityManagerFactoryBuilder builder,
            @Qualifier("defaultDatasource") DataSource routerDataSource) {
        return builder
                .dataSource(routerDataSource)
                .packages("com.staples.cp.matrix.repository")
                .persistenceUnit("routerPU")
                .build();
    }

    @Primary
    @Bean(name = "routerTransactionManager")
    public PlatformTransactionManager mysqlTransactionManager(
            @Qualifier("routerEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }*/
}
