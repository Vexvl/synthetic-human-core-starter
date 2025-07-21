package svs.command.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CommandStatsDto {
    private int totalCommands;
    private int uptimeSeconds;
}