package wolox.reactortraining.services.delegates;

import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamWarningEvent;
import org.springframework.social.twitter.api.Tweet;
import reactor.core.publisher.FluxSink;

public class StreamListenerDelegate implements StreamListener {

    private FluxSink<Tweet> emitter;

    public StreamListenerDelegate(FluxSink<Tweet> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onTweet(Tweet tweet) {
        this.emitter.next(tweet);
    }

    @Override
    public void onDelete(StreamDeleteEvent deleteEvent) {

    }

    @Override
    public void onLimit(int numberOfLimitedTweets) {

    }

    @Override
    public void onWarning(StreamWarningEvent warningEvent) {

    }
}