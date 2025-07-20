package svs.command.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import svs.audit.WeylandWatchingYou;
import svs.command.model.Command;
import svs.command.processor.CommandProcessor;

@Service
@RequiredArgsConstructor
public class CommandServiceImpl implements CommandService{

    private final CommandProcessor commandProcessor;
    @Override
    @WeylandWatchingYou
    public void executeCommand(Command command) {
        commandProcessor.submitCommand(command);
    }
}
