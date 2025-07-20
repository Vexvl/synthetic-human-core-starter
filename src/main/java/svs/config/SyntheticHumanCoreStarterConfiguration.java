package svs.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import svs.audit.AuditProperties;

@Configuration
@EnableConfigurationProperties(AuditProperties.class)
public class SyntheticHumanCoreStarterConfiguration {
}