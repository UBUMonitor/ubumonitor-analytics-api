package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JdbcTemplateFactory {

    private final MultiTenantConnectionProviderImpl provider;

    public JdbcTemplate getJdbcTemplate() {

        DataSource ds = provider.getDataSourceForTenant();

        return new JdbcTemplate(ds);
    }
}
