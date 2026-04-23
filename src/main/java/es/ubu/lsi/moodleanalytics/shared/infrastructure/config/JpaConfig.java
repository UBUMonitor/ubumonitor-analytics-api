package es.ubu.lsi.moodleanalytics.shared.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.moodle.config.MoodleConfig;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.session.CurrentSessionContext;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.database.DynamicRoutingDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class JpaConfig {

    @Bean
    @Primary
    public DataSource dataSource(
        CurrentSessionContext sessionContext,
        MoodleConfig moodleConfig) {

        DynamicRoutingDataSource routingDataSource =
            new DynamicRoutingDataSource(sessionContext, moodleConfig);

        // Bootstrap obligatorio
        HikariDataSource bootstrapDs = new HikariDataSource();
        bootstrapDs.setJdbcUrl("jdbc:h2:mem:bootstrap;DB_CLOSE_DELAY=-1");
        bootstrapDs.setUsername("sa");
        bootstrapDs.setPassword("");
        bootstrapDs.setDriverClassName("org.h2.Driver");

        Map<Object, Object> targets = new HashMap<>();
        targets.put("bootstrap", bootstrapDs);

        routingDataSource.setTargetDataSources(targets);
        routingDataSource.setDefaultTargetDataSource(bootstrapDs);

        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }
}
