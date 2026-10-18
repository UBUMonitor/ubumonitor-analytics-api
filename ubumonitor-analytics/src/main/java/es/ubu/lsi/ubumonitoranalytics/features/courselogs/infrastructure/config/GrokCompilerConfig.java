package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.config;

import io.krakens.grok.api.GrokCompiler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/** Creates a Grok compiler and registers the bundled pattern definitions. */
@Configuration
public class GrokCompilerConfig {
  @Bean
  public GrokCompiler grok() {
    return createCompiler();
  }

  public GrokCompiler createCompiler() {
    Resource resource = new ClassPathResource("patterns/patterns");
    GrokCompiler grokInstance = GrokCompiler.newInstance();
    try (var inputStream = resource.getInputStream()) {
      grokInstance.register(inputStream, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to load Grok patterns", exception);
    }
    return grokInstance;
  }
}
