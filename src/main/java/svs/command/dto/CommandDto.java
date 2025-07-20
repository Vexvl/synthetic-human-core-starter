package svs.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CommandDto {

    @NotBlank
    @Size(max = 1000)
    private final String description;

    @NotBlank
    private final String priority;

    @NotBlank
    @Size(max = 100)
    private final String author;

    @NotBlank
    private final String time;
}