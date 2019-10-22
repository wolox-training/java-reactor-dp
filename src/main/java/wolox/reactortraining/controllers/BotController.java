package wolox.reactortraining.controllers;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
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
import wolox.reactortraining.services.BotService;

@RestController
@RequestMapping("/bots")
@Validated
public class BotController {

    @Autowired
    private BotService botService;

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
        return botService
            .createConversation(names)
            .log();
    }
}
