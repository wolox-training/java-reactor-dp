package wolox.reactortraining.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import wolox.reactortraining.models.User;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {

}
