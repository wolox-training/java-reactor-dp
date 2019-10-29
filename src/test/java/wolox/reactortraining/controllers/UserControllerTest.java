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
import wolox.reactortraining.dtos.UserDto;
import wolox.reactortraining.models.User;
import wolox.reactortraining.repositories.UserRepository;

@RunWith(SpringRunner.class)
@WebFluxTest(UserController.class)
public class UserControllerTest {

    private final String createUserUrl = "/users";
    private final String deleteUserUrl = "/users/{id}";
    private final String getUsers = "/users";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void givenInvalidUserInput_whenCreateIsCalled_thenResponseIsBadRequest() {
        UserDto userDto = new UserDto();

        webTestClient
            .post()
            .uri(createUserUrl)
            .body(BodyInserters.fromObject(userDto))
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    public void givenValidUserInput_whenCreateIsCalled_thenResponseMustBeSuccess() {
        UserDto userDto = new UserDto();
        userDto.setUsername(randomAlphabetic(10));

        when(userRepository.insert(any(User.class)))
            .thenReturn(Mono.just(new User(userDto)));

        webTestClient
            .post()
            .uri(createUserUrl)
            .body(BodyInserters.fromObject(userDto))
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    public void givenNonExistentUser_whenDeleteIsCalled_thenResponseMustBeUserNotFound() {
        when(userRepository.findById(anyString()))
            .thenReturn(Mono.empty());

        webTestClient
            .delete()
            .uri(deleteUserUrl, randomAlphabetic(16))
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    public void givenExistentUser_whenDeleteIsCalled_thenResponseMustBeOk() {
        UserDto userDto = new UserDto();
        userDto.setUsername(randomAlphabetic(10));

        String id = randomAlphabetic(10);

        User user = new User(userDto);

        when(userRepository.findById(id))
            .thenReturn(Mono.just(user));

        when(userRepository.delete(user))
            .thenReturn(Mono.empty());

        webTestClient
            .delete()
            .uri(deleteUserUrl, id)
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    public void givenNoUsers_whenGetUsersIsCalled_thenResponseMustBeEmpty() {
        when(userRepository.findAll())
            .thenReturn(Flux.empty());

        webTestClient
            .get()
            .uri(getUsers)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").exists()
            .jsonPath("$").isArray()
            .jsonPath("$").isEmpty();
    }

    @Test
    public void givenUsersInDB_whenGetUsersIsCalled_thenResponseMustBeEmpty() {
        User mock1 = new User();
        User mock2 = new User();

        when(userRepository.findAll())
            .thenReturn(Flux.just(mock1, mock2));

        webTestClient
            .get()
            .uri(getUsers)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").exists()
            .jsonPath("$").isArray()
            .jsonPath("$").isNotEmpty()
            .jsonPath("$", hasSize(2));
    }
}
