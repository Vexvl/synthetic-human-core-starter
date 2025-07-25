package svs.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditProperties auditProperties;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Around("@annotation(svs.audit.WeylandWatchingYou)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        String argsString = Arrays.toString(args).toLowerCase();

        if (argsString.contains("self-destruct")) {
            log.warn("WARNING: Bishop is attempting self-destruct! Contact Weyland-Yutani immediately!");
            if (auditProperties.isKafkaEnabled()) {
                kafkaTemplate.send(auditProperties.getKafkaTopic(),
                        "!!! CRITICAL EVENT: self-destruct command detected from " + methodName + " !!!");
            }
        }

        log.info("AUDIT: method={}, args={}", methodName, Arrays.toString(args));
        Object result = joinPoint.proceed();
        log.info("AUDIT: result={}", result);

        if (auditProperties.isKafkaEnabled()) {
            String message = String.format("Method: %s, Args: %s, Result: %s",
                    methodName, Arrays.toString(args), result);
            kafkaTemplate.send(auditProperties.getKafkaTopic(), message).exceptionally(ex -> {
                log.error("Failed to send audit message kafka sos", ex);
                return null;
            });
        }

        return result;
    }
}