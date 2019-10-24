package wolox.reactortraining.services;

import static reactor.core.publisher.Mono.error;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import wolox.reactortraining.dtos.BotCreationDto;
import wolox.reactortraining.dtos.BotDto;
import wolox.reactortraining.dtos.TopicDto;
import wolox.reactortraining.dtos.UserDto;
import wolox.reactortraining.exceptions.MatchingTweetsNotFound;
import wolox.reactortraining.models.Topic;
import wolox.reactortraining.models.User;
import wolox.reactortraining.repositories.TopicRepository;
import wolox.reactortraining.repositories.UserRepository;
import wolox.reactortraining.responses.BotResponse;

@Service
public class BotService implements IBotService {

    private static final int VALUE_MAX_MESSAGES = 8; // Value is no include in random generation;
    private static final long VALUE_MIN_MESSAGES = 3; // Added to get random numbers among 3 & 10;

    @Value("${bot.min-message-long}")
    private Integer minMessageLong;

    @Value("${bot.max-message-long}")
    private Integer maxMessageLong;

    @Autowired
    @Qualifier("bot-api")
    private WebClient botWebClient;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TwitterService twitterService;

    private Logger logger = LoggerFactory.getLogger(BotService.class);

    private Random randomGenerator = new Random();

    private static TopicDto createTopicDto(String description) {
        TopicDto topicDto = new TopicDto();
        topicDto.setDescription(description);
        return topicDto;
    }

    private static boolean tweetHasTopics(String tweet, List<String> topics) {
        boolean contains = false;

        for (String topic : topics) {
            if (tweet.contains(topic)) {
                contains = true;
                break;
            }
        }

        return contains;
    }

    private static BotDto createBotDto(String botName, String text) {
        BotDto botDto = new BotDto();
        botDto.setName(botName);
        botDto.setText(text);
        return botDto;
    }

    private static Topic createTopicModel(TopicDto dto, User user) {
        Topic topic = new Topic(dto);
        topic.addUser(user);
        return topic;
    }

    private static String formatResponse(BotResponse response) {
        return String.format("%n-----> { botName:%s } <-----%n%s%n%n", response.getName(),
            response.getResponse());
    }

    @Override
    public Mono<BotDto> create(BotDto botDto) {
        return botWebClient
            .post()
            .uri("/feed")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(botDto), BotDto.class)
            .retrieve()
            .bodyToMono(BotDto.class);
    }

    @Override
    public Mono<BotResponse> talk(String name, Integer length) {
        return botWebClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/took")
                .queryParam("name", name)
                .queryParam("length", length)
                .build())
            .retrieve()
            .bodyToMono(BotResponse.class);
    }

    @Override
    public Mono<Void> createBot(BotCreationDto botCreationDto) {
        Mono<User> userMono = createUser(botCreationDto.getUsername());
        Mono<Void> feedBot = feedBot(botCreationDto.getBotName(), botCreationDto.getTopics());

        return userMono
            .flatMap(
                user -> Mono.zip(createTopics(user, botCreationDto.getTopics()), Mono.just(user)))
            .flatMap(this::addTopicsToUser)
            .then(feedBot);
    }

    @Override
    public Flux<String> createConversation(List<String> names) {
        List<Flux<String>> conversationalFluxes = names
            .stream()
            .map(name -> Flux
                .just(name)
                .flatMap(this::makeBotTalk)
                .repeat(VALUE_MIN_MESSAGES + randomGenerator.nextInt(VALUE_MAX_MESSAGES))
                .map(BotService::formatResponse)
            )
            .collect(Collectors.toList());

        return Flux
            .merge(Flux.merge(conversationalFluxes))
            .log();
    }

    private Mono<User> createUser(String username) {
        UserDto userDto = new UserDto();
        userDto.setUsername(username);

        User user = new User(userDto);

        return userRepository.insert(user);
    }

    private Mono<List<Topic>> createTopics(User user, List<String> topics) {
        return Flux
            .fromIterable(topics)
            .map(BotService::createTopicDto)
            .map(dto -> createTopicModel(dto, user))
            .flatMap(topicRepository::insert)
            .collectList();
    }

    private Mono<Void> feedBot(String botName, List<String> topics) {
        return twitterService
            .getTweetsStreamPipe()
            .map(Tweet::getText)
            .take(500)
            .filter(text -> tweetHasTopics(text, topics))
            .switchIfEmpty(error(MatchingTweetsNotFound::new))
            .collectList()
            .map(stringList -> String.join(" ", stringList))
            .map(text -> createBotDto(botName, text))
            .flatMap(this::create)
            .then();
    }

    private Mono<User> addTopicsToUser(Tuple2<List<Topic>, User> tuple) {
        User user = tuple.getT2();
        List<Topic> topics = tuple.getT1();

        user.addTopics(topics);

        return userRepository.save(user);
    }

    private Mono<BotResponse> makeBotTalk(String botName) {
        int length = randomGenerator.nextInt(maxMessageLong - minMessageLong) + minMessageLong;
        return talk(botName, length);
    }
}
