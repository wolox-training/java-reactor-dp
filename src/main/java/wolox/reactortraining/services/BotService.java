package wolox.reactortraining.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import wolox.reactortraining.dtos.BotDto;
import wolox.reactortraining.responses.BotResponse;

@Service
public class BotService implements IBotService {

    @Autowired
    @Qualifier("bot-api")
    private WebClient botWebClient;

    public Mono<BotDto> create(BotDto botDto) {
        return botWebClient
            .post()
            .uri("/feed")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(botDto), BotDto.class)
            .retrieve()
            .bodyToMono(BotDto.class);
    }

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

}
