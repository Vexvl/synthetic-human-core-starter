package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import svs.command.model.Command;
import svs.command.model.Priority;
import svs.command.processor.CommandProcessor;
import svs.command.service.CommandServiceImpl;

import static org.mockito.Mockito.*;

class CommandServiceImplTest {

    private CommandProcessor commandProcessor;
    private CommandServiceImpl commandService;

    @BeforeEach
    void setup() {
        commandProcessor = Mockito.mock(CommandProcessor.class);
        commandService = new CommandServiceImpl(commandProcessor);
    }

    @Test
    void executeCommand_ShouldCallProcessor() {
        Command command = Command.builder()
                .description("Test")
                .priority(Priority.COMMON)
                .author("Bishop")
                .build();

        commandService.executeCommand(command);

        verify(commandProcessor, times(1)).submitCommand(command);
    }
}
