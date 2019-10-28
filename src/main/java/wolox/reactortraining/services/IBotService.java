package wolox.reactortraining.services;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wolox.reactortraining.dtos.BotCreationDto;
import wolox.reactortraining.dtos.BotDto;
import wolox.reactortraining.responses.BotResponse;

public interface IBotService {

    Mono<BotDto> create(BotDto botDto);

    Mono<BotResponse> talk(String name, Integer length);

    Mono<Void> createBot(BotCreationDto botCreationDto);

    Flux<String> createConversation(List<String> names);

}
