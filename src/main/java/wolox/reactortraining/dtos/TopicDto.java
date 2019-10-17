package wolox.reactortraining.dtos;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.ToString;

@ToString
public class TopicDto {

    @NotEmpty
    @Pattern(regexp = "^\\w+$", message = "Topic must be a single word")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
