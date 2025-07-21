package svs.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private static final String COMMAND_QUEUE_SIZE = "command.queue.size";
    private static final String BISHOP_TOTAL_COMMANDS = "bishop.total.commands";
    private static final String BISHOP_UPTIME_SECONDS = "bishop.uptime.seconds";
    private static final String COMMANDS_BY_AUTHOR = "commands.by.author";

    private final MeterRegistry meterRegistry;
    private final AtomicInteger queueSize = new AtomicInteger(0);
    private final AtomicInteger totalCommands = new AtomicInteger(0);
    private final AtomicInteger uptimeSeconds = new AtomicInteger(0);
    private final Map<String, Counter> authorCounters = new HashMap<>();

    @PostConstruct
    public void init() {
        meterRegistry.gauge(COMMAND_QUEUE_SIZE, queueSize);
        meterRegistry.gauge(BISHOP_TOTAL_COMMANDS, totalCommands);
        meterRegistry.gauge(BISHOP_UPTIME_SECONDS, uptimeSeconds);
    }

    @Scheduled(fixedRate = 1000)
    public void incrementUptime() {
        uptimeSeconds.incrementAndGet();
    }

    public void setQueueSize(int size) {
        queueSize.set(size);
    }

    public void incrementAuthorTask(String author) {
        Counter counter;
        synchronized (authorCounters) {
            counter = authorCounters.computeIfAbsent(author, a ->
                    Counter.builder(COMMANDS_BY_AUTHOR)
                            .tag("author", a)
                            .register(meterRegistry)
            );
        }
        counter.increment();
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