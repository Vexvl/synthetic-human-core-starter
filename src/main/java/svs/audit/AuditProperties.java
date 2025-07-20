package svs.audit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "audit")
public class AuditProperties {
    private boolean kafkaEnabled = false;
    private String kafkaTopic = "audit-topic";
}