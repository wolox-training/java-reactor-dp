package wolox.reactortraining.services;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamingOperations;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import wolox.reactortraining.services.delegates.StreamListenerDelegate;

@Service
public class TwitterService {

    private Logger logger = LoggerFactory.getLogger(TwitterService.class);

    private ConnectableFlux<Tweet> tweetsConnectionFlux;

    @Autowired
    private Twitter twitter;

    public SearchResults searchSpringBoot(String query) {
        return twitter
            .searchOperations()
            .search(query);
    }

    private ConnectableFlux<Tweet> createTweetsStreamPipe() {

        logger.info("-> creating: getTweetsStreamPipe");

        ConnectableFlux<Tweet> tweetsListener = Flux
            .create(streamingEmitter())
            .doOnError(error -> logger.error("Error sampling Twitter stream API", error))
            .publish();

        logger.info("-> created: getTweetsStreamPipe");

        tweetsListener.connect();

        logger.info("-> called: createTweetsStreamPipe");

        return tweetsListener;
    }

    public ConnectableFlux<Tweet> getTweetsStreamPipe() {
        if (tweetsConnectionFlux == null) {
            tweetsConnectionFlux = createTweetsStreamPipe();
        }

        logger.info("-> called: getTweetsStreamPipe");

        return tweetsConnectionFlux;
    }

    private Consumer<FluxSink<Tweet>> streamingEmitter() {
        return emitter -> {
            StreamingOperations streamingOperations = twitter.streamingOperations();

            StreamListener streamListener = new StreamListenerDelegate(emitter);
            List<StreamListener> listeners = Collections.singletonList(streamListener);
            streamingOperations.sample(listeners);
        };
    }
}
