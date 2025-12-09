package moadong.global.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class QuartzDataSourceConfig {

    // 1. spring.datasource.quartz.* 설정을 DataSourceProperties 객체에 바인딩
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.quartz")
    public DataSourceProperties quartzDataSourceProperties() {
        return new DataSourceProperties(); // 빈 DataSourceProperties 객체를 생성하여 설정 바인딩
    }

    // 2. 바인딩된 속성(Properties)을 사용하여 실제 HikariDataSource 빈 생성
    @Bean
    @QuartzDataSource
    public DataSource quartzDataSource(DataSourceProperties quartzDataSourceProperties) {
        // DataSourceProperties의 모든 속성을 HikariDataSource 객체로 옮겨 DataSource를 만듭니다.
        return quartzDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class) // HikariCP 사용 명시
                .build();
    }
}
