package moadong.global.config;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class QuartzConfig {

    // Spring Bean 주입을 Quartz Job 내에서 가능하게 하는 팩토리
    // Job클래스에서 스프링컨테이너를 인식하고 관련된 빈을 주입받아 사용하려면 필요함
    public static class AutowiringSpringBeanJobFactory extends org.springframework.scheduling.quartz.SpringBeanJobFactory {
        private final ApplicationContext context;

        public AutowiringSpringBeanJobFactory(ApplicationContext context) {
            this.context = context;
        }

        @Override
        protected Object createJobInstance(final org.quartz.spi.TriggerFiredBundle bundle) throws Exception {
            final Object job = super.createJobInstance(bundle);
            context.getAutowireCapableBeanFactory().autowireBean(job);
            return job;
        }
    }

    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        return new AutowiringSpringBeanJobFactory(applicationContext);
    }

    // SchedulerFactoryBean: Quartz 스케줄러를 초기화하고 설정
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(@Qualifier("quartzDataSource")DataSource dataSource, JobFactory jobFactory) throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource); // Spring DB 연결 정보를 Quartz에 전달
        factory.setJobFactory(jobFactory);
        factory.setOverwriteExistingJobs(true); // 기존에 예약된 Job을 덮어쓸 수 있도록 설정
        factory.setApplicationContextSchedulerContextKey("applicationContext");

        // 1. Quartz Properties 로드 (클러스터링 및 JobStore 설정)
        factory.setQuartzProperties(quartzProperties());

        return factory;
    }

    // 2. quartz.properties 파일 설정
    private Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }
}
