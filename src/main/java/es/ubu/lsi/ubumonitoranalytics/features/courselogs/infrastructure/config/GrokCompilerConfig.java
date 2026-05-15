package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.config;

import io.krakens.grok.api.GrokCompiler;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

@Configuration
public class GrokCompilerConfig {
    @Bean
    @SneakyThrows
    public GrokCompiler grok() {
        Resource resource = new ClassPathResource("patterns/patterns");
        GrokCompiler grokInstance = GrokCompiler.newInstance();
        grokInstance.register(resource.getInputStream(), StandardCharsets.UTF_8);
        return grokInstance;
    }
}
