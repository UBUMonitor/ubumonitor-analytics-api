package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.config;

import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.MultiTenantConnectionProviderImpl;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.TenantIdentifierResolver;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultiTenantConfig {

    @Bean
    public MultiTenantConnectionProvider<String> multiTenantConnectionProvider(
            CurrentSessionContext currentSessionContext,
            MoodleConfig moodleConfig) {

        return new MultiTenantConnectionProviderImpl(
            currentSessionContext,
            moodleConfig
        );
    }

    @Bean
    public CurrentTenantIdentifierResolver<String> tenantIdentifierResolver(
        CurrentSessionContext currentSessionContext) {

        return new TenantIdentifierResolver(currentSessionContext);
    }
}
