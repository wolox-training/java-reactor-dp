package wolox.reactortraining.models;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import wolox.reactortraining.dtos.UserDto;

@Document
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class User {

    @Id
    private String id;

    private String username;

    @DBRef(lazy = true)
    private List<Topic> topics = new ArrayList<>();

    public User() {
    }

    public User(UserDto dto) {
        this.username = dto.getUsername();
    }

    public void addTopics(List<Topic> topics) {
        this.topics.addAll(topics);
    }
}

