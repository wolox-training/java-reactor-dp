package wolox.reactortraining.models;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import wolox.reactortraining.dtos.UserDto;

@Document
@Getter
@Setter
@NoArgsConstructor
@ToString
public class User {

    @Id
    private String id;

    private String username;

    @DBRef
    private List<Topic> topics;

    public static User fromDto(UserDto dto) {
        User user = new User();
        user.username = dto.getUsername();

        return user;
    }
}

