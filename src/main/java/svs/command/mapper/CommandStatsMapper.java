package svs.command.mapper;

import org.springframework.stereotype.Component;
import svs.command.dto.CommandStatsDto;

@Component
public class CommandStatsMapper {

    public static CommandStatsDto toCommandStatsDto(int totalCommands, int uptimeSeconds) {
        return CommandStatsDto.builder()
                .totalCommands(totalCommands)
                .uptimeSeconds(uptimeSeconds)
                .build();
    }
}