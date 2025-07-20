package svs.command.processor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import svs.command.model.Command;
import svs.command.model.Priority;
import svs.exception.CommandQueueOverflowException;
import svs.metrics.MetricsService;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
@Slf4j
public class CommandProcessor {

    private final MetricsService metricsService;

    private final BlockingQueue<Runnable> commandQueue = new LinkedBlockingQueue<>(100);
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, 4, 10, TimeUnit.SECONDS, commandQueue);

    @PostConstruct
    public void init() {
        metricsService.bindQueueSize();
    }

    public void submitCommand(Command command) {
        metricsService.setQueueSize(commandQueue.size());
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
            throw new CommandQueueOverflowException("Queue is full");
        }
    }
}
