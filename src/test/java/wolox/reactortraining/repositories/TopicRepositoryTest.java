package wolox.reactortraining.repositories;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;

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
public class TopicRepositoryTest {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Before
    public void setup() {
        topicRepository
            .deleteAll()
            .block();
    }

    @Test
    public void givenNoPersistedTopics_whenFindAllIsCalled_thenReturnEmptyResultSet() {
        Flux<Topic> findAllFlux = topicRepository.findAll();

        StepVerifier
            .create(findAllFlux)
            .expectSubscription()
            .expectComplete()
            .verify();
    }

    @Test
    public void givenPersistedTopicsInDB_whenFindAllIsCalled_thenReturnNonEmptyResultSet() {
        TopicDto dto = new TopicDto();
        dto.setDescription(randomAlphanumeric(6));

        Topic topic = new Topic(dto);

        Topic persistedTopic = topicRepository
            .save(topic)
            .block();

        Flux<Topic> findAllFlux = topicRepository.findAll();

        StepVerifier
            .create(findAllFlux)
            .expectSubscription()
            .expectNext(persistedTopic)
            .expectComplete()
            .verify();
    }

    @Test
    public void whenInsertNewTopic_thenItMustBeReturnedWithAnId() {
        TopicDto dto = new TopicDto();
        dto.setDescription(randomAlphanumeric(6));

        Topic topic = new Topic(dto);

        Mono<Topic> insertTopicMono = topicRepository.insert(topic);

        StepVerifier
            .create(insertTopicMono)
            .expectSubscription()
            .assertNext(next -> {
                assertThat(next).isNotNull();
                assertThat(next.getId()).isNotNull();
                assertThat(next.getDescription()).isEqualTo(topic.getDescription());
            })
            .expectComplete()
            .verify();
    }

    @Test
    public void whenInsertNewTopicAssociatedWithAndUser_thenItMustBeReturnedComplete() {
        TopicDto dto = new TopicDto();
        dto.setDescription(randomAlphanumeric(6));

        Topic topic = new Topic(dto);

        UserDto userDto = new UserDto();
        userDto.setUsername(randomAlphabetic(5));
        User user = userRepository.insert(new User(userDto)).block();

        topic.addUser(user);

        Mono<Topic> insertTopicMono = topicRepository.insert(topic);

        StepVerifier
            .create(insertTopicMono)
            .expectSubscription()
            .assertNext(next -> {
                assertThat(next).isNotNull();
                assertThat(next.getId()).isNotNull();
                assertThat(next.getUsers()).isNotEmpty();
                assertThat(next.getUsers()).hasSize(1);
                assertThat(next.getDescription()).isEqualTo(topic.getDescription());
                assertThat(next.getUsers().get(0).getId()).isNotNull();
                assertThat(next.getUsers().get(0).getId()).isEqualTo(user.getId());
                assertThat(next.getUsers().get(0)).isEqualTo(user);
            })
            .expectComplete()
            .verify();
    }

}
