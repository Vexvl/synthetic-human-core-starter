package svs.command.service;

import svs.command.model.Command;

public interface CommandService {
    void executeCommand(Command command);
}