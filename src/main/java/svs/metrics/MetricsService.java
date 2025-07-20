package svs.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    private final AtomicInteger queueSize = new AtomicInteger(0);
    private final ConcurrentHashMap<String, AtomicInteger> authorTaskCount = new ConcurrentHashMap<>();

    public void bindQueueSize() {
        meterRegistry.gauge("command.queue.size", queueSize);
    }

    public void setQueueSize(int size) {
        queueSize.set(size);
    }

    public void incrementAuthorTask(String author) {
        authorTaskCount.computeIfAbsent(author, a -> {
            AtomicInteger counter = new AtomicInteger(0);
            meterRegistry.gauge("commands.by.author", List.of(Tag.of("author", a)), counter);
            return counter;
        }).incrementAndGet();
    }
}