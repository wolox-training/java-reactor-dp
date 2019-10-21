package wolox.reactortraining.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import wolox.reactortraining.models.Topic;

@Repository
public interface TopicRepository extends ReactiveMongoRepository<Topic, String> {

}
