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
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableTransactionManagement // Spring의 트랜잭션 관리를 활성화함 : AOP를 기반으로 메서드 단위 트랜잭션을 관리할 수 있게 한다. @Transactional 어노테이션을 사용해 메서드나 클래스에 트랜잭션 범위를 지정할 수 있다.
@EnableJpaRepositories( // Spring Data JPA 리포지토리(Repository)를 활성화하고 설정함 : 지정된 패키지(basePackages)에서 JPA 리포지토리 인터페이스를 검색하고, Spring Data JPA가 해당 인터페이스에 대한 구현체를 생성한다.
        basePackages = "com.example.multidb.repository", // mysql의 repository 패키지
        entityManagerFactoryRef = "mysqlEntityManagerFactory", // EntityManager의 이름
        transactionManagerRef = "mysqlTransactionManager" // 트랜잭션 매니저의 이름
)
public class MysqlDatasourceConfig {

    @Value("${spring.jpa.properties.hibernate.dialect.mysql}")
    private String dialect;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.mysql")  // application.yml에 작성된 DB와 관련된 설정 값들의 접두사
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.mysql.configuration") // DB와 관련된 설정값들의 접두사에 .configuration을 붙여준다.
    public DataSource mysqlDatasource() {
        return mysqlDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "mysqlEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        DataSource dataSource = mysqlDatasource();
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("hibernate.dialect", dialect);
        objectObjectHashMap.put("hibernate.hbm2ddl.auto", "update"); // 자동 생성 property : ddl-auto는 안됨
        objectObjectHashMap.put("hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName()); // CamelCase → snake_case 자동 변환
        objectObjectHashMap.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName()); // @Table, @Column이 없을 때 자동 변환
        return builder
                .dataSource(dataSource)
                .packages("com.example.multidb.entity") // 첫번째 DB와 관련된 엔티티들이 있는 패키지(폴더) 경로, 여러 경로 시 : .packages("com.example.multidb.entity","com.example.multidb.aaa.entity")
                .persistenceUnit("mysqlEntityManager")
                .properties(objectObjectHashMap)
                .build();
    }

    @Bean(name = "mysqlTransactionManager")
    @Primary
    public PlatformTransactionManager primaryTransactionManager(
            final @Qualifier("mysqlEntityManagerFactory") LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean
    ) {
        return new JpaTransactionManager(localContainerEntityManagerFactoryBean.getObject());
    }
}


/**
 * * EntityManagerFactory
 *  * 역할
 *      * EntityManager 객체를 생성하는 팩토리 역할
 *      * 영속성 컨텍스트(Persistence Context)를 관리함
 *      * EntityManager : DB와의 실제작업(삽입, 삭제, 수정, 조회 등)을 수행
 *  * 특징
 *      * 애플리케이션 전체에서 공유 가능함.(Thread-safe) but, EntityManager은 Thread-safe하지 않으며 요청마다 새로운 인스턴스를 생성해야함.
 *      * 애플리케이션 시작 시 한 번만 생성되고, 애플리케이션 종료 시 닫아야함.
 *
 * * TransactionManager
 *  * 역할
 *      * DB 트랜잭션을 관리
 *      * @Transactional을 통해 선언적으로 트랜잭션을 제어할 수 있음.
 *  * 특징
 *      * 트랜잭션 제어 가능. : 트랜잭션 시작, 커밋, 롤백. 트랜잭션의 범위는 보통 하나의 비즈니스 로직 메서드
 *      * 트랜잭션은 보통 하나의 요청(스레드)에 연결되며, ThreadLocal을 통해 트랜잭션 상태를 관리함.
 *      * Spting 추상화 : JPA, JDBC, Hibernate 등 다양한 기술의 트랜잭션 관리자를 통합적으로 제공함.
 *
 *
 *
 * => 공 : DB와의 상호작용에 중요한 부분들이다.
 * => 차 : EntityManagerFactory (JPA 엔티티의 영속성 관리) , TransactionManager (데이터베이스 트랜잭션 범위 제어)
 * */