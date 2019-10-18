package wolox.reactortraining.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BotDto {

    @NotBlank
    private String name;

    @NotEmpty
    @Size(min = 20)
    private String text;

}
