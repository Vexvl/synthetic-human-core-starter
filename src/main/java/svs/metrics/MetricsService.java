package svs.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger queueSize = new AtomicInteger(0);
    private final AtomicInteger totalCommands = new AtomicInteger(0);
    private final AtomicInteger uptimeSeconds = new AtomicInteger(0);
    private final ConcurrentHashMap<String, AtomicInteger> authorTaskCount = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        meterRegistry.gauge("command.queue.size", queueSize);
        meterRegistry.gauge("bishop.total.commands", totalCommands);
        meterRegistry.gauge("bishop.uptime.seconds", uptimeSeconds);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                uptimeSeconds::incrementAndGet, 1, 1, TimeUnit.SECONDS);
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

    public int incrementTotalCommands() {
        return totalCommands.incrementAndGet();
    }

    public int getTotalCommands() {
        return totalCommands.get();
    }

    public int getUptimeSeconds() {
        return uptimeSeconds.get();
    }
}