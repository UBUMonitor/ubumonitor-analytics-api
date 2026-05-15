package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.config;


import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
@RequiredArgsConstructor
public class StringToGrokConverter implements Converter<String, Grok> {
    private final GrokCompiler grokInstance;


    @Override
    public Grok convert(@NonNull String source) {
        return grokInstance.compile(source);
    }
}
