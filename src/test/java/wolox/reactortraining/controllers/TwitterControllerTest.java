package wolox.reactortraining.controllers;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import wolox.reactortraining.services.TwitterService;

@RunWith(SpringRunner.class)
@WebFluxTest(TwitterController.class)
public class TwitterControllerTest {

    private final String searchTweeterUrl = "/twitter";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TwitterService twitterService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenAStringQuery_whenTwitterIsCalled_thenResponseMustBeASearchResult()
        throws JsonProcessingException {
        String query = randomAlphabetic(10);

        List<Tweet> tweets = Arrays.asList(
            new Tweet(1L, randomAlphabetic(30), Date.from(Instant.now()), randomAlphabetic(5),
                randomAlphabetic(10), 1L, 1L, randomAlphabetic(2), randomAlphabetic(10)),
            new Tweet(1L, randomAlphabetic(30), Date.from(Instant.now()), randomAlphabetic(5),
                randomAlphabetic(10), 1L, 1L, randomAlphabetic(2), randomAlphabetic(10)),
            new Tweet(1L, randomAlphabetic(30), Date.from(Instant.now()), randomAlphabetic(5),
                randomAlphabetic(10), 1L, 1L, randomAlphabetic(2), randomAlphabetic(10)),
            new Tweet(1L, randomAlphabetic(30), Date.from(Instant.now()), randomAlphabetic(5),
                randomAlphabetic(10), 1L, 1L, randomAlphabetic(2), randomAlphabetic(10))
        );

        SearchResults searchResults = new SearchResults(tweets, null);

        when(twitterService.searchSpringBoot(query))
            .thenReturn(searchResults);

        webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path(searchTweeterUrl)
                .queryParam("query", query)
                .build()
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .json(objectMapper.writeValueAsString(searchResults));
    }
}