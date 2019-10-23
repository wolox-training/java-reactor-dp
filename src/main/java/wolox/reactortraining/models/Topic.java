package wolox.reactortraining.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import wolox.reactortraining.dtos.TopicDto;

@Document
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Topic {

    @Id
    private String id;

    private String description;

    @DBRef(lazy = true)
    @JsonIgnore
    private List<User> users = new ArrayList<>();

    public Topic() {
    }

    public Topic(TopicDto dto) {
        this.description = dto.getDescription();
    }

    public void addUser(User user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }
}
