package svs.command.processor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import svs.command.model.Command;
import svs.command.model.Priority;
import svs.exception.CommandQueueOverflowException;
import svs.metrics.MetricsService;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Component
@Slf4j
public class CommandProcessor {

    private final MetricsService metricsService;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final BlockingQueue<Runnable> commandQueue = new LinkedBlockingQueue<>(100);
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, 4, 10, TimeUnit.SECONDS, commandQueue);
    private final AtomicBoolean cooldownMode = new AtomicBoolean(false);
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    private final Map<String, Runnable> cheatCodes = Map.of(
            "iddqd", () -> log.info("God mode activated!"),
            "idkfa", () -> log.info("All weapons granted!")
    );

    @PostConstruct
    public void init() {
        log.info("      [ BISHOP SYSTEM STARTED ]       ");
        log.info("      [ Synthetic Human Core ]        ");
        log.info("      [   Weyland-Yutani     ]        ");

        scheduler.scheduleAtFixedRate(() -> {
            int size = commandQueue.size();
            log.info("[LOGS SYSTEM ACTIVATED] Current queue size: {}", size);
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void submitCommand(Command command) {
        if (executor.isShutdown()) {
            log.warn("Rejecting command, executor is shutting down.");
            return;
        }
        metricsService.setQueueSize(commandQueue.size());
        String description = command.getDescription().toLowerCase();

        Runnable cheat = cheatCodes.get(description);
        if (cheat != null) {
            cheat.run();
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
        try {
            log.info("Executing CRITICAL command: {}", command);
            metricsService.incrementAuthorTask(command.getAuthor());
        } catch (Exception e) {
            log.error("Error executing CRITICAL command: {}", command, e);
        }
    }

    private void enqueueCommand(Command command) {
        try {
            executor.submit(() -> {
                try {
                    log.info("Executing COMMON command: {}", command);
                    metricsService.incrementAuthorTask(command.getAuthor());
                } catch (Exception e) {
                    log.error("Error executing COMMON command: {}", command, e);
                }
            });
        } catch (RejectedExecutionException e) {
            handleQueueOverflow();
            throw new CommandQueueOverflowException("Queue is full");
        }
    }

    private void handleQueueOverflow() {
        if (cooldownMode.compareAndSet(false, true)) {
            log.warn("!!!!!! Too many requests !!!!!! Cooling down the command processor for 10 seconds...");
            scheduler.schedule(() -> {
                cooldownMode.set(false);
                log.info("Cooldown ended. Command processor is active again.");
            }, 10, TimeUnit.SECONDS);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (!isShuttingDown.compareAndSet(false, true)) {
            log.info("Shutdown already in progress");
            return;
        }
        log.info("Shutting down CommandProcessor...");
        executor.shutdown();
        scheduler.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Forcing shutdown of CommandProcessor...");
                executor.shutdownNow();
            }
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Forcing shutdown of Scheduler...");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted during shutdown, forcing shutdown now.");
            executor.shutdownNow();
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}