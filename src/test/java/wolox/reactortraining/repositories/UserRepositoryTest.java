package wolox.reactortraining.repositories;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wolox.reactortraining.dtos.TopicDto;
import wolox.reactortraining.dtos.UserDto;
import wolox.reactortraining.models.Topic;
import wolox.reactortraining.models.User;

@RunWith(SpringRunner.class)
@DataMongoTest
public class UserRepositoryTest {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Before
    public void setup() {
        userRepository
            .deleteAll()
            .block();
    }

    @Test
    public void givenNoPersistedUsers_whenFindAllIsCalled_thenReturnEmptyResultSet() {
        Flux<User> findAllFlux = userRepository.findAll();

        StepVerifier
            .create(findAllFlux)
            .expectSubscription()
            .expectComplete()
            .verify();
    }

    @Test
    public void givenPersistedUsersInDB_whenFindAllIsCalled_thenReturnNonEmptyResultSet() {
        UserDto dto = new UserDto();
        dto.setUsername(randomAlphanumeric(6));

        User user = new User(dto);

        User persistedUser = userRepository
            .save(user)
            .block();

        Flux<User> findAllFlux = userRepository.findAll();

        StepVerifier
            .create(findAllFlux)
            .expectSubscription()
            .expectNext(persistedUser)
            .expectComplete()
            .verify();
    }

    @Test
    public void whenInsertNewUser_thenItMustBeReturnedWithAnId() {
        UserDto dto = new UserDto();
        dto.setUsername(randomAlphanumeric(6));

        User user = new User(dto);

        Mono<User> insertUserMono = userRepository.insert(user);

        StepVerifier
            .create(insertUserMono)
            .expectSubscription()
            .assertNext(next -> {
                assertThat(next).isNotNull();
                assertThat(next.getId()).isNotNull();
                assertThat(next.getUsername()).isEqualTo(user.getUsername());
            })
            .expectComplete()
            .verify();
    }

    @Test
    public void givenAnUserWithATopic_whenFindByAllIsCalled_thenItMustBeReturnedComplete() {
        UserDto userDto = new UserDto();
        userDto.setUsername(randomAlphabetic(9));

        TopicDto topicDto = new TopicDto();
        topicDto.setDescription(randomAlphabetic(4));

        Topic topic = topicRepository
            .insert(new Topic(topicDto))
            .block();

        User user = new User(userDto);
        user.addTopics(Collections.singletonList(topic));

        Mono<User> saveMono = userRepository.insert(user);

        StepVerifier
            .create(saveMono)
            .expectSubscription()
            .assertNext(next -> {
                assertThat(next).isNotNull();
                assertThat(next.getId()).isNotNull();
                assertThat(next.getTopics()).isNotEmpty();
                assertThat(next.getTopics()).hasSize(1);
                assertThat(next.getTopics().get(0)).isEqualTo(topic);
                assertThat(next.getTopics().get(0).getId()).isNotNull();
            })
            .expectComplete()
            .verify();

    }
}
