package wolox.reactortraining.services;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static wolox.reactortraining.TestUtils.mockResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import wolox.reactortraining.beans.WebClientBean;
import wolox.reactortraining.dtos.BotCreationDto;
import wolox.reactortraining.dtos.BotDto;
import wolox.reactortraining.models.Topic;
import wolox.reactortraining.models.User;
import wolox.reactortraining.repositories.TopicRepository;
import wolox.reactortraining.repositories.UserRepository;
import wolox.reactortraining.responses.BotResponse;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BotService.class, WebClientBean.class})
@TestPropertySource(properties = {
    "bot.min-message-long=20",
    "bot.max-message-long=60",
    "api.bots.uri=http://localhost:7000"
})
public class BotServiceTest {

    private static final int PORT = 7000;

    @Autowired
    private BotService botService;

    @MockBean
    private TopicRepository topicRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TwitterService twitterService;

    private BotDto dummyCreateDto;
    private MockWebServer fakeServer;
    private ObjectMapper objectMapper;
    private Random random = new Random();

    @Before
    public void setup() throws IOException {
        fakeServer = new MockWebServer();
        fakeServer.start(PORT);

        objectMapper = new ObjectMapper();

        dummyCreateDto = new BotDto();
        dummyCreateDto.setName(randomAlphabetic(10));
        dummyCreateDto.setText(randomAlphabetic(20));
    }

    @After
    public void tearDown() throws IOException {
        fakeServer.shutdown();
    }

    @Test
    public void givenValidInputDto_whenCreateBasicBotIsCalledAndServerFail_thenCreateMustFail()
        throws JsonProcessingException {
        MockResponse mockResponse = mockResponse(
            objectMapper,
            MediaType.APPLICATION_JSON_VALUE,
            400,
            null
        );

        fakeServer.enqueue(mockResponse);

        Mono<BotDto> createMono = botService.create(dummyCreateDto);

        StepVerifier
            .create(createMono)
            .expectSubscription()
            .expectNextCount(0)
            .verifyError();
    }

    @Test
    public void givenValidInputDto_whenCreateBasicBotIsCalledAndServerSucceeds_thenCreateMustSuccess()
        throws JsonProcessingException {
        MockResponse mockResponse = mockResponse(
            objectMapper,
            MediaType.APPLICATION_JSON_VALUE,
            200,
            dummyCreateDto
        );

        fakeServer.enqueue(mockResponse);

        Mono<BotDto> createMono = botService.create(dummyCreateDto);

        StepVerifier
            .create(createMono)
            .expectSubscription()
            .expectNext(dummyCreateDto)
            .expectNextCount(0)
            .expectComplete()
            .verify();
    }

    @Test
    public void givenNameAndLength_whenTalksIsCalled_thenResponseMustBeNotEmpty()
        throws JsonProcessingException, InterruptedException {
        BotResponse botResponse = new BotResponse();
        botResponse.setName(randomAlphabetic(10));
        botResponse.setResponse(randomAlphabetic(50));

        MockResponse mockResponse = mockResponse(
            objectMapper,
            MediaType.APPLICATION_JSON_VALUE,
            200,
            botResponse
        );

        fakeServer.enqueue(mockResponse);

        Mono<BotResponse> talk = botService.talk(randomAlphabetic(10), 50);

        StepVerifier
            .create(talk)
            .expectSubscription()
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response).hasFieldOrProperty("name");
                assertThat(response).hasFieldOrProperty("response");
                assertThat(response.getName()).isEqualTo(botResponse.getName());
                assertThat(response.getResponse()).isEqualTo(botResponse.getResponse());
            });
    }

    @Test
    public void givenAValidBotCreationDto_whenCreateBotIsCalled_thenCreateNewBot()
        throws JsonProcessingException {
        MockResponse mockResponse = mockResponse(
            objectMapper,
            MediaType.APPLICATION_JSON_VALUE,
            HttpStatus.OK.value(),
            dummyCreateDto
        );

        fakeServer.enqueue(mockResponse);

        User dummyUser = new User();
        dummyUser.setUsername(randomAlphabetic(10));

        Topic dummyTopic = new Topic();
        dummyTopic.setDescription(randomAlphabetic(10));

        String username = randomAlphabetic(6);
        String botName = randomAlphabetic(6);
        String knownTopic = randomAlphabetic(4);
        List<String> topicList = Arrays.asList(
            knownTopic,
            randomAlphabetic(5),
            randomAlphabetic(6)
        );

        BotCreationDto botCreationDto = new BotCreationDto();
        botCreationDto.setUsername(username);
        botCreationDto.setBotName(botName);
        botCreationDto.setTopics(topicList);

        when(userRepository.insert(any(User.class))).thenReturn(Mono.just(dummyUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(dummyUser));
        when(topicRepository.insert(any(Topic.class))).thenReturn(Mono.just(dummyTopic));

        Tweet[] randomTweets = new Tweet[]{
            new Tweet(1L, knownTopic + " " + randomAlphabetic(30), Date.from(Instant.now()),
                randomAlphabetic(5), randomAlphabetic(10), 1L, 1L, randomAlphabetic(2),
                randomAlphabetic(10)),
            new Tweet(1L, randomAlphabetic(30), Date.from(Instant.now()), randomAlphabetic(5),
                randomAlphabetic(10), 1L, 1L, randomAlphabetic(2), randomAlphabetic(10)),
            new Tweet(1L, knownTopic + " " + randomAlphabetic(30), Date.from(Instant.now()),
                randomAlphabetic(5), randomAlphabetic(10), 1L, 1L, randomAlphabetic(2),
                randomAlphabetic(10)),
            new Tweet(1L, randomAlphabetic(30), Date.from(Instant.now()), randomAlphabetic(5),
                randomAlphabetic(10), 1L, 1L, randomAlphabetic(2), randomAlphabetic(10)),
        };

        TestPublisher<Tweet> testPublisher = TestPublisher.create();

        Flux<Tweet> flux = testPublisher.flux();
        ConnectableFlux<Tweet> connectionFlux = flux.publish();

        when(twitterService.getTweetsStreamPipe()).thenReturn(connectionFlux);

        Mono<Void> bot = botService.createBot(botCreationDto);

        StepVerifier
            .create(bot)
            .then(() -> {
                connectionFlux.connect();

                for (int i = 0; i < 600; i++) {
                    int position = random.nextInt(4);
                    testPublisher.next(randomTweets[position]);
                }
            })
            .verifyComplete();
    }

    @Test
    public void givenBotNames_whenCreateConversationIsCalled_thenResponseMustNotBeError()
        throws JsonProcessingException {
        List<String> botNames = Arrays.asList(
            randomAlphabetic(5),
            randomAlphabetic(5),
            randomAlphabetic(5)
        );

        for (int i = 0; i < 200; i++) {
            BotResponse botResponse = new BotResponse();
            botResponse.setName(botNames.get(i % 3));
            botResponse.setResponse(randomAlphabetic(60));

            MockResponse mockResponse = mockResponse(
                objectMapper,
                MediaType.APPLICATION_JSON_VALUE,
                200,
                botResponse
            );

            fakeServer.enqueue(mockResponse);
        }

        Flux<String> conversation = botService.createConversation(botNames);

        StepVerifier
            .create(conversation)
            .expectSubscription()
            .thenConsumeWhile(response -> {
                assertThat(response).isNotNull();
                assertThat(response.contains(botNames.get(0))
                    || response.contains(botNames.get(1))
                    || response.contains(botNames.get(2)))
                    .isTrue();
                return true;
            })
            .expectComplete()
            .verify();
    }
}