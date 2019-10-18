package wolox.reactortraining.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wolox.reactortraining.dtos.BotCreationDto;
import wolox.reactortraining.dtos.BotDto;
import wolox.reactortraining.responses.BotResponse;
import wolox.reactortraining.services.IBotService;

@RestController
@RequestMapping("/bots")
@Validated
public class BotController {

    private static final int VALUE_MAX_MESSAGES = 8; // Value is no include in random generation;
    private static final long VALUE_MIN_MESSAGES = 3; // Added to get random numbers among 3 & 10;

    @Value("${bot.min-message-long}")
    private Integer minMessageLong;

    @Value("${bot.max-message-long}")
    private Integer maxMessageLong;

    @Autowired
    private IBotService botService;

    private Random randomGenerator = new Random();

    @PostMapping("/")
    public Mono<Void> create(@Valid @RequestBody BotDto botDto) {
        return Mono
            .just(botDto)
            .flatMap(botService::create)
            .then()
            .log();
    }

    @GetMapping("/")
    public Mono<BotResponse> talk(@NotEmpty @RequestParam("name") String name,
        @RequestParam(value = "length", defaultValue = "60") Integer length) {
        return botService
            .talk(name, length)
            .log();
    }

    @PostMapping("/create")
    public Mono<Void> createBot(@Valid @RequestBody BotCreationDto botCreationDto) {
        return botService
            .createBot(botCreationDto)
            .log();
    }

    @GetMapping(
        value = "/conversation",
        produces = {
            MediaType.APPLICATION_STREAM_JSON_VALUE,
            MediaType.TEXT_EVENT_STREAM_VALUE
        })
    public Flux<String> conversation(
        @Valid @RequestParam("names") @NotEmpty List<@NotBlank String> names) {
        List<Flux<String>> conversationalFluxes = new ArrayList<>();

        for (String name : names) {
            Flux<String> botChat = Flux
                .just(name)
                .flatMap(botName -> botService.talk(botName,
                    randomGenerator.nextInt(maxMessageLong - minMessageLong) + minMessageLong))
                .repeat(VALUE_MIN_MESSAGES + randomGenerator.nextInt(VALUE_MAX_MESSAGES))
                .map(response ->
                    String.format(
                        "%n-----> { botName:%s } <-----%n%s%n%n",
                        response.getName(),
                        response.getResponse()
                    )
                );

            conversationalFluxes.add(botChat);
        }

        return Flux
            .merge(Flux.merge(conversationalFluxes))
            .log();
    }
}
