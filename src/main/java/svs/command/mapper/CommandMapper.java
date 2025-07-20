package svs.command.mapper;

import org.springframework.stereotype.Component;
import svs.command.dto.CommandDto;
import svs.command.model.Command;
import svs.command.model.Priority;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Component
public class CommandMapper {

    public static Command toCommand(CommandDto dto) {
        return Command.builder()
                .description(dto.getDescription())
                .priority(parsePriority(dto.getPriority()))
                .author(dto.getAuthor())
                .time(parseTime(dto.getTime()))
                .build();
    }

    private static Priority parsePriority(String priorityStr) {
        try {
            return Priority.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid priority value: " + priorityStr);
        }
    }

    private static LocalDateTime parseTime(String timeStr) {
        try {
            return LocalDateTime.parse(timeStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format (expected ISO-8601) " + timeStr);
        }
    }
}