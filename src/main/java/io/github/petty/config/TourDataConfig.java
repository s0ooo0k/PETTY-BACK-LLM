package io.github.petty.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * TourDataConfig
 * 
 * Aiven MySQL에 저장된 Pet Tour Data를 Spring JPA를 통하여 연결
 * 
 * - DB 연결 설정
 * - JPA Entity 클래스 위치 지정
 * - JPA Repository 위치 지정
 * - 트랜잭션(읽기 전용)
 *
 */

@Configuration
@EnableTransactionManagement // @Transactional 동작 설정
@EnableJpaRepositories(
        basePackages = "io.github.petty.tour.repository",
        entityManagerFactoryRef = "tourEntityManagerFactory",
        transactionManagerRef = "tourTransactionManager"
)

public class TourDataConfig {
    /**
     * Aiven MySql 연결 DataSource 생성
     */
    @Primary
    @Bean(name="tourDataSource")
    @ConfigurationProperties(prefix="spring.datasource.aiven")
    public DataSource tourDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * JPA에서 사용할 EntityManagerFactory 설정
     * JPA DB 연동 - Entity 관리
     */
    @Primary
    @Bean(name="tourEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tourEntityManagerFactory(
            // datasource 2개 이상일 경우 명시
            @Qualifier("tourDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder
    ) {
        return builder.dataSource(dataSource)
                .packages("io.github.petty.tour.entity")
                .persistenceUnit("tour")
                .build();
    }

    /**
     * Transaction Manager 설정
     */
    @Primary
    @Bean(name="tourTransactionManager")
    public PlatformTransactionManager tourTransactionManager(
            @Qualifier("tourEntityManagerFactory") EntityManagerFactory emf
    ) {
        return new JpaTransactionManager(emf);
    }
}
