package wolox.reactortraining.services;

import static reactor.core.publisher.Mono.error;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wolox.reactortraining.dtos.BotCreationDto;
import wolox.reactortraining.dtos.BotDto;
import wolox.reactortraining.dtos.TopicDto;
import wolox.reactortraining.dtos.UserDto;
import wolox.reactortraining.exceptions.MatchingTwitsNotFound;
import wolox.reactortraining.models.Topic;
import wolox.reactortraining.models.User;
import wolox.reactortraining.repositories.TopicRepository;
import wolox.reactortraining.repositories.UserRepository;
import wolox.reactortraining.responses.BotResponse;

@Service
public class BotService implements IBotService {

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
        Mono<Void> createBot = createBot(botCreationDto.getBotName(), botCreationDto.getTopics());

        return userMono
            .flatMap(user ->
                Mono.zip(createTopics(user, botCreationDto.getTopics()), Mono.just(user)))
            .flatMap(tuple -> {
                User user = tuple.getT2();
                List<Topic> topics = tuple.getT1();

                user.addTopics(topics);

                return userRepository.save(user);
            })
            .then(createBot);
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
            .map(description -> {
                TopicDto topicDto = new TopicDto();
                topicDto.setDescription(description);
                return topicDto;
            })
            .map(dto -> {
                Topic topic = new Topic(dto);
                topic.addUser(user);
                return topic;
            })
            .flatMap(topicRepository::insert)
            .collectList();
    }

    private Mono<Void> createBot(String botName, List<String> topics) {
        return twitterService
            .getTweetsStreamPipe()
            .map(Tweet::getText)
            .take(500)
            .filter(text -> {
                for (String topic : topics) {
                    if (text.contains(topic)) {
                        return true;
                    }
                }

                return false;
            })
            .switchIfEmpty(error(MatchingTwitsNotFound::new))
            .collectList()
            .map(stringList -> String.join(" ", stringList))
            .map(text -> {
                BotDto botDto = new BotDto();
                botDto.setName(botName);
                botDto.setText(text);
                return botDto;
            })
            .flatMap(this::create)
            .then();
    }

}
