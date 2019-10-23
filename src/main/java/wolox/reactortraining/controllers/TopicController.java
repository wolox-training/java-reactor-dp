package wolox.reactortraining.controllers;

import static reactor.core.publisher.Mono.error;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wolox.reactortraining.dtos.TopicDto;
import wolox.reactortraining.exceptions.TopicNotFound;
import wolox.reactortraining.models.Topic;
import wolox.reactortraining.repositories.TopicRepository;

@RestController
@RequestMapping("/topics")
public class TopicController {

    @Autowired
    private TopicRepository topicRepository;

    @PostMapping
    public Mono<Void> create(@Valid @RequestBody TopicDto topicDto) {
        return Mono
            .just(topicDto)
            .flatMap(dto -> topicRepository.insert(new Topic(dto)))
            .then()
            .log();
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable("id") String id) {
        return topicRepository
            .findById(id)
            .flatMap(topicRepository::delete)
            .switchIfEmpty(error(new TopicNotFound()))
            .log();
    }

    @GetMapping
    public Flux<Topic> getTopics(@RequestParam(value = "size", defaultValue = "10") Integer size) {
        return topicRepository
            .findAll()
            .take(size)
            .log();
    }

}
