package wolox.reactortraining.services;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.social.twitter.api.SearchOperations;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamingOperations;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.ConnectableFlux;
import wolox.reactortraining.services.delegates.StreamListenerDelegate;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TwitterService.class})
public class TwitterServiceTest {

    @Autowired
    private TwitterService twitterService;

    @MockBean
    private Twitter twitter;

    @Mock
    private SearchOperations searchOperations;

    @Mock
    private StreamingOperations streamingOperations;

    @Captor
    private ArgumentCaptor<List<StreamListener>> streamListeners;

    @Test
    public void givenAQuery_whenSearchSpringBootIsCalled_thenReturnNonEmptyResult() {
        String query = randomAlphabetic(5);

        SearchResults searchResults = new SearchResults(Collections.emptyList(), null);

        when(twitter.searchOperations())
            .thenReturn(searchOperations);

        when(searchOperations.search(query))
            .thenReturn(searchResults);

        twitterService.searchSpringBoot(query);

        verify(searchOperations, times(1)).search(query);
        verify(twitter, times(1)).searchOperations();
    }

    @Test
    public void whenGetTweetsStreamPipeIsCalled_thenReturnSameInstance() {
        when(twitter.streamingOperations())
            .thenReturn(streamingOperations);

        ConnectableFlux<Tweet> tweetsStreamPipe1 = twitterService.getTweetsStreamPipe();
        ConnectableFlux<Tweet> tweetsStreamPipe2 = twitterService.getTweetsStreamPipe();

        assertThat(tweetsStreamPipe1).isEqualTo(tweetsStreamPipe2);

        verify(streamingOperations, times(1)).sample(streamListeners.capture());

        List<StreamListener> listeners = streamListeners.getValue();

        assertThat(listeners).hasSize(1);
        assertThat(listeners.get(0)).isInstanceOf(StreamListenerDelegate.class);
    }
}
