package wolox.reactortraining.controllers;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wolox.reactortraining.dtos.UserDto;
import wolox.reactortraining.exceptions.UserNotFound;
import wolox.reactortraining.models.User;
import wolox.reactortraining.repositories.UserRepository;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED, reason = "User created")
    public Mono<Void> create(@Valid @RequestBody UserDto userDto) {
        return Mono
            .just(userDto)
            .flatMap(dto -> userRepository.insert(new User(dto)))
            .then()
            .log();
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable("id") String id) {
        return userRepository
            .findById(id)
            .switchIfEmpty(Mono.error(new UserNotFound()))
            .flatMap(userRepository::delete)
            .log();
    }

    @GetMapping
    public Flux<User> getUsers(@RequestParam(value = "size", defaultValue = "10") Integer size) {
        return userRepository
            .findAll()
            .take(size)
            .log();
    }
}
