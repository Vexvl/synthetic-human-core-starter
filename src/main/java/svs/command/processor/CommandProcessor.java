package svs.command.processor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import svs.command.model.Command;
import svs.command.model.Priority;
import jakarta.annotation.PreDestroy;
import svs.exception.CommandQueueOverflowException;
import svs.metrics.MetricsService;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Component
@Slf4j
public class CommandProcessor {

    private final MetricsService metricsService;
    private final BlockingQueue<Runnable> commandQueue = new LinkedBlockingQueue<>(100);
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, 4, 10, TimeUnit.SECONDS, commandQueue);
    private final AtomicBoolean cooldownMode = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        log.info("      [ BISHOP SYSTEM STARTED ]       ");
        log.info("      [ Synthetic Human Core ]        ");
        log.info("      [   Weyland-Yutani     ]        ");
    }

    public void submitCommand(Command command) {
        metricsService.setQueueSize(commandQueue.size());
        String description = command.getDescription().toLowerCase();

        //читы
        if ("iddqd".equals(description)) {
            log.info("God mode activated!");
            return;
        }
        if ("idkfa".equals(description)) {
            log.info("All weapons granted!");
            return;
        }

        metricsService.incrementTotalCommands();

        if (cooldownMode.get() && command.getPriority() != Priority.CRITICAL) {
            log.warn("System is cooling down. Rejecting command: {}", command.getDescription());
            return;
        }

        if (command.getPriority() == Priority.CRITICAL) {
            executeImmediately(command);
        } else {
            enqueueCommand(command);
        }
    }

    private void executeImmediately(Command command) {
        log.info("Executing CRITICAL command: {}", command);
        metricsService.incrementAuthorTask(command.getAuthor());
    }

    private void enqueueCommand(Command command) {
        try {
            executor.submit(() -> {
                log.info("Executing COMMON command: {}", command);
                metricsService.incrementAuthorTask(command.getAuthor());
            });
        } catch (RejectedExecutionException e) {
            handleQueueOverflow();
            throw new CommandQueueOverflowException("Queue is full");
        }
    }

    private void handleQueueOverflow() {
        if (cooldownMode.compareAndSet(false, true)) {
            log.warn("!!!!!!Too many requests!!!!!!!" +
                    " Cooling down the command processor for 10 seconds...");
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                cooldownMode.set(false);
                log.info("Cooldown ended. Command processor is active again.");
            }, 10, TimeUnit.SECONDS);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down CommandProcessor...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Forcing shutdown of CommandProcessor...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted during shutdown, forcing shutdown now.");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}