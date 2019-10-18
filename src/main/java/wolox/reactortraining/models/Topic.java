package wolox.reactortraining.models;

import java.util.List;
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
public class Topic {

    @Id
    private String id;

    private String description;

    @DBRef
    private List<User> users;

    public Topic() {
    }

    public Topic(TopicDto dto) {
        this.description = dto.getDescription();
    }
}
