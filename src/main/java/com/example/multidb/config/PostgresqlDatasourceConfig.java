package com.example.multidb.config;

import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.multidb.postgresql.repository", // mysql의 repository 패키지
        entityManagerFactoryRef = "postgresqlEntityManagerFactory",
        transactionManagerRef = "postgresqlTransactionManager"
)
public class PostgresqlDatasourceConfig {

    @Value("${spring.jpa.properties.hibernate.dialect.postgresql}")
    private String dialect;

    @Bean
    @ConfigurationProperties("spring.datasource.postgresql")
    public DataSourceProperties postgresqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.postgresql.configuration") // DB와 관련된 설정값들의 접두사에 .configuration을 붙여준다.
    public DataSource postgresqlDatasource() {
        return postgresqlDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "postgresqlEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean secondEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        DataSource dataSource = postgresqlDatasource();
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("hibernate.dialect", dialect);
        objectObjectHashMap.put("hibernate.hbm2ddl.auto", "update"); // 자동 생성 property : ddl-auto는 안됨
        objectObjectHashMap.put("hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName()); // CamelCase → snake_case 자동 변환
        objectObjectHashMap.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName()); // @Table, @Column이 없을 때 자동 변환
        return builder
                .dataSource(dataSource)
                .packages("com.example.multidb.postgresql.entity") // 두번째 DB와 관련된 엔티티들이 있는 패키지(폴더) 경로
                .persistenceUnit("postgresqlEntityManager")
                .properties(objectObjectHashMap)
                .build();
    }

    @Bean(name = "postgresqlTransactionManager")
    public PlatformTransactionManager secondTransactionManager(
            final @Qualifier("postgresqlEntityManagerFactory") LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean
    ) {
        return new JpaTransactionManager(localContainerEntityManagerFactoryBean.getObject());
    }
}
