package wolox.reactortraining.dtos;

import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BotDto {

    @NotEmpty
    private String name;

    @NotEmpty
    private String text;

}
