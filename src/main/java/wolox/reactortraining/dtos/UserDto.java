package wolox.reactortraining.dtos;

import javax.validation.constraints.NotBlank;
import lombok.ToString;

@ToString
public class UserDto {

    @NotBlank(message = "Username must be provided")
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
