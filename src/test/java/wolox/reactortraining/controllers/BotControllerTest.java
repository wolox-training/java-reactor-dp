package wolox.reactortraining.controllers;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wolox.reactortraining.dtos.BotCreationDto;
import wolox.reactortraining.dtos.BotDto;
import wolox.reactortraining.responses.BotResponse;
import wolox.reactortraining.services.BotService;

@RunWith(SpringRunner.class)
@WebFluxTest(BotController.class)
public class BotControllerTest {

    private final String createBotUri = "/bots/create";
    private final String createUri = "/bots/";
    private final String conversationUri = "/bots/conversation";
    private final String talkUri = "/bots/";

    @MockBean
    private BotService botService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void givenInvalidInput_whenCreateIsCalled_thenReturnBadRequest() {
        webTestClient
            .post()
            .uri(createUri)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.empty())
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    public void givenValidInput_whenCreateIsCalled_thenReturnSuccess() {
        BotDto botDto = new BotDto();
        botDto.setName(randomAlphabetic(5));
        botDto.setText(randomAlphabetic(20));

        when(botService.create(any()))
            .thenReturn(Mono.empty());

        webTestClient
            .post()
            .uri(createUri)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromObject(botDto))
            .exchange()
            .expectStatus().isOk()
            .expectBody().isEmpty();
    }

    @Test
    public void givenInvalidInput_whenTalkIsCalled_thenReturnBadRequest() {
        webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(talkUri)
                    .build()
            )
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    public void givenValidInput_whenTalkIsCalled_thenReturnSuccess() {
        BotResponse botResponse = new BotResponse();
        botResponse.setName(randomAlphabetic(5));
        botResponse.setResponse(randomAlphabetic(60));

        when(botService.talk(any(), any()))
            .thenReturn(Mono.just(botResponse));

        webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(talkUri)
                    .queryParam("name", randomAlphabetic(5))
                    .build()
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(BotResponse.class)
            .isEqualTo(botResponse);
    }

    @Test
    public void givenInvalidInput_whenCreateFullIsCalled_thenResponseMustBeBadRequest() {
        BotCreationDto botDto = new BotCreationDto();

        webTestClient
            .post()
            .uri(createBotUri)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromObject(botDto))
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    public void givenValidInput_whenCreateFullIsCalled_thenResponseMustBeCreated() {
        BotCreationDto botDto = new BotCreationDto();
        botDto.setUsername(randomAlphabetic(6));
        botDto.setBotName(randomAlphabetic(10));

        botDto.setTopics(Arrays
            .asList(
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10)
            )
        );

        when(botService.createBot(any()))
            .thenReturn(Mono.empty());

        webTestClient
            .post()
            .uri(createBotUri)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromObject(botDto))
            .exchange()
            .expectStatus().isOk()
            .expectBody().isEmpty();
    }

    @Test
    public void givenInvalidInput_whenConversationIsCalled_thenResponseMustBeBadRequest() {
        webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(conversationUri)
                    .build()
            )
            .accept(MediaType.APPLICATION_STREAM_JSON)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void givenValidInput_whenConversationIsCalled_thenResponseMustBeSuccess() {
        List<String> conversations = Arrays
            .asList("conversation_1", "conversation_2", "conversation_3", "conversation_4");

        Flux<String> stringFlux = Flux.fromIterable(conversations);

        when(botService.createConversation(any()))
            .thenReturn(stringFlux);

        webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(conversationUri)
                    .queryParam("names", Arrays.asList(
                        randomAlphabetic(10),
                        randomAlphabetic(10)
                    ))
                    .build()
            )
            .accept(MediaType.APPLICATION_STREAM_JSON)
            .exchange()
            .expectStatus().isOk();
    }
}