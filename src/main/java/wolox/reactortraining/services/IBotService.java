package wolox.reactortraining.services;

import reactor.core.publisher.Mono;
import wolox.reactortraining.dtos.BotDto;
import wolox.reactortraining.models.BotResponse;

public interface IBotService {

    Mono<BotDto> create(BotDto botDto);

    Mono<BotResponse> talk(String name, Integer length);

}
