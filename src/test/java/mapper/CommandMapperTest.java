package mapper;

import org.junit.jupiter.api.Test;
import svs.command.dto.CommandDto;
import svs.command.mapper.CommandMapper;
import svs.command.model.Command;
import svs.command.model.Priority;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommandMapperTest {

    @Test
    void toCommand_ShouldMapCorrectly() {
        CommandDto dto = CommandDto.builder()
                .description("Test command")
                .priority("COMMON")
                .author("Bishop")
                .time("2025-07-25T12:05:00")
                .build();

        Command command = CommandMapper.toCommand(dto);

        assertEquals("Test command", command.getDescription());
        assertEquals(Priority.COMMON, command.getPriority());
        assertEquals("Bishop", command.getAuthor());
        assertEquals(LocalDateTime.parse("2025-07-25T12:05:00"), command.getTime());
    }

    @Test
    void toCommand_ShouldThrowOnInvalidPriority() {
        CommandDto dto = CommandDto.builder()
                .description("Test command")
                .priority("INVALID")
                .author("Bishop")
                .time("2025-07-25T12:05:00")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CommandMapper.toCommand(dto));

        assertTrue(ex.getMessage().contains("Invalid priority value"));
    }

    @Test
    void toCommand_ShouldThrowOnInvalidTime() {
        CommandDto dto = CommandDto.builder()
                .description("Test command")
                .priority("COMMON")
                .author("Bishop")
                .time("fffffffffff")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CommandMapper.toCommand(dto));

        assertTrue(ex.getMessage().contains("Invalid time format"));
    }
}