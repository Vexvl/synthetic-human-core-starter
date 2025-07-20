package processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import svs.command.model.Command;
import svs.command.model.Priority;
import svs.command.processor.CommandProcessor;
import svs.metrics.MetricsService;

import static org.mockito.Mockito.*;

class CommandProcessorTest {

    private MetricsService metricsService;
    private CommandProcessor commandProcessor;

    @BeforeEach
    void setup() {
        metricsService = mock(MetricsService.class);
        commandProcessor = new CommandProcessor(metricsService);
        commandProcessor.init();
    }

    @Test
    void submitCommand_Critical_ExecutesImmediately() {
        Command criticalCommand = Command.builder()
                .priority(Priority.CRITICAL)
                .author("Bishop")
                .description("asdvcx")
                .build();

        commandProcessor.submitCommand(criticalCommand);

        verify(metricsService, times(1)).incrementAuthorTask("Bishop");
    }

    @Test
    void submitCommand_Common_Enqueues() throws InterruptedException {
        Command commonCommand = Command.builder()
                .priority(Priority.COMMON)
                .author("AAA")
                .description("asdf")
                .build();

        commandProcessor.submitCommand(commonCommand);

        Thread.sleep(100);

        verify(metricsService, atLeastOnce()).incrementAuthorTask("AAA");
    }
}