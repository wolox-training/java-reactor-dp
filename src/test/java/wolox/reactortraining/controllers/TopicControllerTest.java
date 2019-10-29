package wolox.reactortraining.controllers;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wolox.reactortraining.dtos.TopicDto;
import wolox.reactortraining.models.Topic;
import wolox.reactortraining.repositories.TopicRepository;

@RunWith(SpringRunner.class)
@WebFluxTest(TopicController.class)
public class TopicControllerTest {

    private final String createTopicUrl = "/topics";
    private final String deleteTopic = "/topics/{id}";
    private final String getTopics = "/topics";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TopicRepository topicRepository;

    @Test
    public void givenInvalidTopicInput_whenCreateIsCalled_thenResponseIsBadRequest() {
        TopicDto topicDto = new TopicDto();

        webTestClient
            .post()
            .uri(createTopicUrl)
            .body(BodyInserters.fromObject(topicDto))
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    public void givenValidUserInput_whenCreateIsCalled_thenResponseMustBeSuccess() {
        TopicDto topicDto = new TopicDto();
        topicDto.setDescription(randomAlphabetic(10));

        when(topicRepository.insert(any(Topic.class)))
            .thenReturn(Mono.just(new Topic(topicDto)));

        webTestClient
            .post()
            .uri(createTopicUrl)
            .body(BodyInserters.fromObject(topicDto))
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    public void givenNonExistentTopic_whenDeleteIsCalled_thenResponseMustBeTopicNotFound() {
        when(topicRepository.findById(anyString()))
            .thenReturn(Mono.empty());

        webTestClient
            .delete()
            .uri(deleteTopic, randomAlphabetic(16))
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    public void givenExistentTopicInDB_whenDeleteIsCalled_thenResponseMustBeOk() {
        TopicDto topicDto = new TopicDto();
        topicDto.setDescription(randomAlphabetic(10));

        String id = randomAlphabetic(16);

        Topic topic = new Topic(topicDto);

        when(topicRepository.findById(id))
            .thenReturn(Mono.just(topic));

        when(topicRepository.delete(topic))
            .thenReturn(Mono.empty());

        webTestClient
            .delete()
            .uri(deleteTopic, id)
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    public void givenNoTopics_whenGetTopicsIsCalled_thenResponseMustBeEmpty() {
        when(topicRepository.findAll())
            .thenReturn(Flux.empty());

        webTestClient
            .get()
            .uri(getTopics)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").exists()
            .jsonPath("$").isArray()
            .jsonPath("$").isEmpty();
    }

    @Test
    public void givenTopicsInDB_whenGetTopicsIsCalled_thenResponseMustBeEmpty() {
        Topic mock1 = new Topic();
        Topic mock2 = new Topic();

        when(topicRepository.findAll())
            .thenReturn(Flux.just(mock1, mock2));

        webTestClient
            .get()
            .uri(getTopics)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").exists()
            .jsonPath("$").isArray()
            .jsonPath("$").isNotEmpty()
            .jsonPath("$", hasSize(2));
    }
}